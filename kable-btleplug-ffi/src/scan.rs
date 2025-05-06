use std::collections::HashMap;

use crate::cancellation_handle::CancellationHandle;
use btleplug::api::{Central, CentralEvent, ScanFilter};
use tokio_stream::StreamExt;
use tokio_util::sync::CancellationToken;

/// As a temporary hack to avoid some ffi work, UUIDs are passed as strings
#[uniffi::export(callback_interface)]
#[async_trait::async_trait]
pub trait ScanCallback: Send + Sync {
    async fn manufacturer_data_adertisement(
        &self,
        id: String,
        manufacturer_data: HashMap<u16, Vec<u8>>,
    );
    async fn service_data_advertisement(&self, id: String, service_data: HashMap<String, Vec<u8>>);
    async fn services_advertisement(&self, id: String, services: Vec<String>);
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
                    Some(event) = events.next() =>
                        handle_event(&*callbacks, event).await
                }
            }
        });
    });
    handle
}

async fn handle_event(callbacks: &dyn ScanCallback, event: CentralEvent) {
    match event {
        CentralEvent::ManufacturerDataAdvertisement {
            id,
            manufacturer_data,
        } => {
            callbacks
                .manufacturer_data_adertisement(id.to_string(), manufacturer_data)
                .await;
        }
        CentralEvent::ServiceDataAdvertisement { id, service_data } => {
            callbacks
                .service_data_advertisement(
                    id.to_string(),
                    service_data
                        .into_iter()
                        .map(|(k, v)| (k.to_string(), v))
                        .collect(),
                )
                .await;
        }
        CentralEvent::ServicesAdvertisement { id, services } => {
            callbacks
                .services_advertisement(
                    id.to_string(),
                    services.into_iter().map(|uuid| uuid.to_string()).collect(),
                )
                .await;
        }
        _ => {}
    }
}
