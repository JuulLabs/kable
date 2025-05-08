use crate::cancellation_handle::CancellationHandle;
use crate::get_adapter;
use crate::peripheral_id::PeripheralId;
use crate::peripheral_properties::PeripheralProperties;
use crate::uuid::Uuid;
use btleplug::api::{Central, CentralEvent, Peripheral as _};
use std::sync::Arc;
use tokio_stream::StreamExt;
use tokio_util::sync::CancellationToken;

#[uniffi::export(callback_interface)]
#[async_trait::async_trait]
pub trait PeripheralCallbacks: Send + Sync {
    fn connected(&self);
    fn disconnected(&self);
    fn notification(&self, uuid: Uuid, data: Vec<u8>);
}

#[derive(Clone, uniffi::Object)]
pub struct Peripheral {
    id: Arc<PeripheralId>,
    platform: btleplug::platform::Peripheral,
    cancellation: CancellationHandle,
}

#[uniffi::export(async_runtime = "tokio")]
impl Peripheral {
    async fn connect(&self) -> bool {
        self.platform.connect().await.is_ok()
    }
    async fn disconnect(&self) -> bool {
        self.platform.disconnect().await.is_ok()
    }

    async fn discover_services(&self) -> bool {
        self.platform.discover_services().await.is_ok()
    }

    async fn properties(&self) -> PeripheralProperties {
        PeripheralProperties::new(
            self.id.clone(),
            self.platform.properties().await.unwrap().unwrap(),
        )
    }
}

impl Drop for Peripheral {
    fn drop(&mut self) {
        self.cancellation.cancel();
    }
}

#[uniffi::export(async_runtime = "tokio")]
pub async fn get_peripheral(
    id: Arc<PeripheralId>,
    callbacks: Box<dyn PeripheralCallbacks>,
) -> Peripheral {
    let adapter = get_adapter().await;
    let mut events = adapter.events().await.unwrap();
    let peripheral = if let Ok(peripheral) = adapter.peripheral(&id.platform).await {
        peripheral
    } else {
        let _ = adapter.add_peripheral(&id.platform).await;
        adapter.peripheral(&id.platform).await.unwrap()
    };
    let mut notifications = peripheral.notifications().await.unwrap();

    let platform_id = id.platform.clone();
    let token = CancellationToken::new();
    let handle = CancellationHandle::new(token.clone());
    std::thread::spawn(move || {
        let rt = tokio::runtime::Builder::new_current_thread()
            .enable_all()
            .build()
            .unwrap();

        rt.block_on(async move {
            loop {
                tokio::select! {
                    _ = token.cancelled() => {
                        break;
                    },
                    Some(event) = events.next() => match event {
                        CentralEvent::DeviceConnected(connected) =>
                            if connected == platform_id { callbacks.connected() }
                        CentralEvent::DeviceDisconnected(disconnected) =>
                            if disconnected == platform_id { callbacks.disconnected() }
                        _ => {}
                    },
                    Some(notification) = notifications.next() =>
                        callbacks.notification(notification.uuid.into(), notification.value)
                }
            }
        });
    });

    Peripheral {
        id,
        platform: peripheral,
        cancellation: handle,
    }
}
