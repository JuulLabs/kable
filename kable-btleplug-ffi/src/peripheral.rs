use crate::cancellation_handle::CancellationHandle;
use crate::characteristic::Characteristic;
use crate::descriptor::Descriptor;
use crate::get_adapter;
use crate::peripheral_id::PeripheralId;
use crate::peripheral_properties::PeripheralProperties;
use crate::service::Service;
use crate::uuid::Uuid;
use crate::write_type::WriteType;
use crate::Result;
use btleplug::api::{Central, CentralEvent, Peripheral as _};
use std::sync::Arc;
use tokio_stream::StreamExt;
use tokio_util::sync::CancellationToken;

#[uniffi::export(callback_interface)]
#[async_trait::async_trait]
pub trait PeripheralCallbacks: Send + Sync {
    fn connected(&self);
    fn disconnected(&self);
    async fn notification(&self, uuid: Uuid, data: Vec<u8>);
}

#[derive(Clone, uniffi::Object)]
pub struct Peripheral {
    id: Arc<PeripheralId>,
    platform: btleplug::platform::Peripheral,
    cancellation: CancellationHandle,
}

#[uniffi::export(async_runtime = "tokio")]
impl Peripheral {
    async fn properties(&self) -> PeripheralProperties {
        PeripheralProperties::new(
            self.id.clone(),
            self.platform.properties().await.unwrap().unwrap(),
        )
    }

    async fn connect(&self) -> bool {
        self.platform.connect().await.is_ok()
    }
    async fn disconnect(&self) -> bool {
        self.platform.disconnect().await.is_ok()
    }

    async fn discover_services(&self) -> bool {
        self.platform.discover_services().await.is_ok()
    }
    async fn services(&self) -> Vec<Service> {
        self.platform
            .services()
            .into_iter()
            .map(|x| x.into())
            .collect()
    }

    async fn read(&self, characteristic: Characteristic) -> Result<Vec<u8>> {
        self.platform
            .read(&characteristic.into())
            .await
            .map_err(|err| err.into())
    }
    async fn write(
        &self,
        characteristic: Characteristic,
        data: Vec<u8>,
        write_type: WriteType,
    ) -> Result<()> {
        self.platform
            .write(&characteristic.into(), &data, write_type.into())
            .await
            .map_err(|err| err.into())
    }

    async fn read_descriptor(&self, descriptor: Descriptor) -> Result<Vec<u8>> {
        self.platform
            .read_descriptor(&descriptor.into())
            .await
            .map_err(|err| err.into())
    }
    async fn write_descriptor(&self, descriptor: Descriptor, data: Vec<u8>) -> Result<()> {
        self.platform
            .write_descriptor(&descriptor.into(), &data)
            .await
            .map_err(|err| err.into())
    }

    async fn subscribe(&self, characteristic: Characteristic) -> Result<()> {
        self.platform
            .subscribe(&characteristic.into())
            .await
            .map_err(|err| err.into())
    }
    async fn unsubscribe(&self, characteristic: Characteristic) -> Result<()> {
        self.platform
            .unsubscribe(&characteristic.into())
            .await
            .map_err(|err| err.into())
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

    let peripherals = adapter.peripherals().await.unwrap();
    let peripheral = if let Ok(peripheral) = adapter.peripheral(&id.platform).await {
        // Already present in the adapter. This path should get hit whenever there's a peripheral
        // created from a scan event/advertisement.
        peripheral
    } else {
        // None of the non-Android platforms support [Adapter::add_peripheral], so we can't use that
        // to eagerly get dummy peripheral. As a very, very sad workaround we need to wait until the
        // peripheral shows up on its own.
        // TODO: This should support a cancellation handle somehow. We probably need to
        //       pre-create in Kotlin and pass into this function (bleh).
        let buffer: btleplug::platform::Peripheral;
        loop {
            tokio::select! {
                Some(event) = events.next() => match event {
                    CentralEvent::DeviceDiscovered(device) |
                    CentralEvent::DeviceUpdated(device) => {
                        if (device == id.platform) {
                            buffer = adapter.peripheral(&device).await.unwrap();
                            break;
                        }
                    },
                    _ => {}
                },
            }
        }
        buffer
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
                        callbacks.notification(notification.uuid.into(), notification.value).await
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
