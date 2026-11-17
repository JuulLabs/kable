use crate::cancellation_handle::CancellationHandle;
use crate::peripheral_properties::PeripheralProperties;
use btleplug::api::{Central, CentralEvent, CentralState, Peripheral, ScanFilter};
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
                        // May fail if the adapter is powered off (e.g. computer went to sleep);
                        // ignore, as scanning has already stopped in that case.
                        let _ = adapter.stop_scan().await;
                        break;
                    },
                    Some(event) = events.next() => match event {
                        CentralEvent::DeviceDiscovered(id) =>
                            handle_event(&adapter, &*callbacks, id).await,
                        CentralEvent::DeviceUpdated(id) =>
                            handle_event(&adapter, &*callbacks, id).await,
                        // The system stops scanning when the adapter powers off (e.g. computer
                        // goes to sleep or Bluetooth is toggled off) and does not resume it when
                        // the adapter powers back on. Restart scanning so that this scan resumes
                        // emitting advertisements: https://github.com/JuulLabs/kable/issues/1152
                        CentralEvent::StateUpdate(CentralState::PoweredOn) => {
                            let _ = adapter.start_scan(ScanFilter::default()).await;
                        }
                        _ => {}
                    }
                }
            }
        });
    });
    handle
}

async fn handle_event(adapter: &Adapter, callbacks: &dyn ScanCallback, id: PeripheralId) {
    // The peripheral (or its properties) may no longer be available (e.g. the adapter powered off
    // after the event was emitted, clearing the adapter's peripherals). Skip the event rather than
    // panicking (which would break the scan's event loop).
    let Ok(peripheral) = adapter.peripheral(&id).await else {
        return;
    };
    let Ok(Some(properties)) = peripheral.properties().await else {
        return;
    };
    let properties = PeripheralProperties::new(Arc::new(id.into()), properties);
    callbacks.update(properties).await;
}
