use std::collections::HashMap;

use crate::cancellation_handle::CancellationHandle;
use crate::uuid::Uuid;
use btleplug::api::{Central, CentralEvent, Peripheral, ScanFilter};
use btleplug::platform::{Adapter, PeripheralId};
use tokio_stream::StreamExt;
use tokio_util::sync::CancellationToken;

/// ID is a UUID on Apple, and a mac-address on Linux and Windows.
#[uniffi::export(callback_interface)]
#[async_trait::async_trait]
pub trait ScanCallback: Send + Sync {
    async fn update(&self, peripheral: PeripheralProperties);
}

#[derive(uniffi::Record, Clone)]
pub struct PeripheralProperties {
    pub id: String,
    pub local_name: Option<String>,
    pub tx_power_level: Option<i16>,
    pub rssi: Option<i16>,
    pub manufacturer_data: HashMap<u16, Vec<u8>>,
    pub service_data: HashMap<Uuid, Vec<u8>>,
    pub services: Vec<Uuid>,
    pub class: Option<u32>,
}

#[uniffi::export(async_runtime = "tokio")]
pub async fn scan(callbacks: Box<dyn ScanCallback>) -> CancellationHandle {
    let token = CancellationToken::new();
    let handle = CancellationHandle::new(token.clone());

    let adapter = crate::get_adapter().await;
    let mut events = adapter.events().await.unwrap();
    adapter.start_scan(ScanFilter::default()).await.unwrap();
    std::thread::spawn(move || {
        let rt = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();

        rt.block_on(async move {
            loop {
                tokio::select! {
                    _ = token.cancelled() => {
                        adapter.stop_scan().await.unwrap();
                        break;
                    },
                    Some(event) = events.next() => match event {
                        CentralEvent::DeviceDiscovered(id) =>
                            handle_event(&adapter, &*callbacks, id).await,
                        CentralEvent::DeviceUpdated(id) =>
                            handle_event(&adapter, &*callbacks, id).await,
                        _ => {}
                    }
                }
            }
        });
    });
    handle
}

async fn handle_event(adapter: &Adapter, callbacks: &dyn ScanCallback, id: PeripheralId) {
    let peripheral = adapter.peripheral(&id).await.unwrap().properties().await.unwrap().unwrap();
    let properties = PeripheralProperties {
        id: id.to_string(),
        local_name: peripheral.local_name,
        tx_power_level: peripheral.tx_power_level,
        rssi: peripheral.rssi,
        manufacturer_data: peripheral.manufacturer_data,
        service_data: peripheral.service_data.into_iter()
            .map(|(uuid, bytes)| (uuid.into(), bytes))
            .collect(),
        services: peripheral.services.into_iter().map(|uuid| uuid.into()).collect(),
        class: peripheral.class,
    };
    callbacks.update(properties).await;
}
