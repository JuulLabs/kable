use crate::cancellation_handle::CancellationHandle;
use crate::peripheral_properties::PeripheralProperties;
use btleplug::api::{Central, CentralEvent, Peripheral, ScanFilter};
use btleplug::platform::{Adapter, PeripheralId};
use std::sync::Arc;
use tokio_stream::StreamExt;
use tokio_util::sync::CancellationToken;

#[uniffi::export(callback_interface)]
#[async_trait::async_trait]
pub trait ScanCallback: Send + Sync {
    async fn update(&self, peripheral: PeripheralProperties);
}

#[uniffi::export(async_runtime = "tokio")]
pub async fn scan(callbacks: Box<dyn ScanCallback>) -> CancellationHandle {
    let token = CancellationToken::new();
    let handle = CancellationHandle::from_token(token.clone());

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
    let peripheral = adapter
        .peripheral(&id)
        .await
        .unwrap()
        .properties()
        .await
        .unwrap()
        .unwrap();
    let properties = PeripheralProperties::new(Arc::new(id.into()), peripheral);
    callbacks.update(properties).await;
}
