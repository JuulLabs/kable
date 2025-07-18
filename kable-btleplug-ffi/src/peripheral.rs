use crate::Result;
use crate::cancellation_handle::CancellationHandle;
use crate::characteristic::Characteristic;
use crate::descriptor::Descriptor;
use crate::peripheral_id::PeripheralId;
use crate::peripheral_properties::PeripheralProperties;
use crate::service::Service;
use crate::uuid::Uuid;
use crate::write_type::WriteType;
use crate::{Error, get_adapter};
use btleplug::api::{Central, CentralEvent, Peripheral as _, ScanFilter};
use std::sync::Arc;
use std::time::Duration;
use tokio::time::timeout;
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
    callbacks: Arc<Box<dyn PeripheralCallbacks>>,
    cancellation: CancellationHandle,
}

impl Peripheral {
    async fn get_platform(&self) -> Result<btleplug::platform::Peripheral> {
        get_adapter()
            .await
            .peripheral(&self.id.platform)
            .await
            .map_err(Into::into)
    }

    async fn platform_connect(
        &self,
        platform: btleplug::platform::Peripheral,
        cancellation_handle: Arc<CancellationHandle>,
    ) -> Result<()> {
        if platform.is_connected().await? {
            return Ok(());
        }

        let token = cancellation_handle.token();
        tokio::select! {
            _ = token.cancelled() => return Err(Error::Cancelled),
            _ = platform.connect() => {}
        }

        let callbacks = self.callbacks.clone();
        let mut notifications = match platform.notifications().await {
            Ok(notifications) => notifications,
            Err(e) => {
                let _ = timeout(Duration::from_secs(1), platform.disconnect()).await;
                return Err(e.into());
            }
        };

        std::thread::spawn(move || {
            let rt = tokio::runtime::Builder::new_current_thread()
                .enable_all()
                .build()
                .unwrap();

            rt.block_on(async move {
                loop {
                    tokio::select! {
                        _ = token.cancelled() => break,
                        Some(notification) = notifications.next() =>
                            callbacks.notification(notification.uuid.into(), notification.value).await
                    }
                }
            });
        });

        Ok(())
    }
}

#[uniffi::export(async_runtime = "tokio")]
impl Peripheral {
    #[uniffi::constructor]
    pub fn new(id: Arc<PeripheralId>, callbacks: Box<dyn PeripheralCallbacks>) -> Self {
        let callbacks = Arc::new(callbacks);
        let token = CancellationToken::new();

        let peripheral = Self {
            id: id.clone(),
            callbacks: callbacks.clone(),
            cancellation: CancellationHandle::from_token(token.clone()),
        };

        std::thread::spawn(|| {
            let rt = tokio::runtime::Builder::new_current_thread()
                .enable_all()
                .build()
                .unwrap();

            rt.block_on(async move {
                let adapter = get_adapter().await;
                let mut events = adapter.events().await.unwrap();
                loop {
                    tokio::select! {
                        _ = token.cancelled() => {
                            break;
                        },
                        Some(event) = events.next() => match event {
                            CentralEvent::DeviceConnected(connected) =>
                                if connected == id.platform { callbacks.connected() }
                            CentralEvent::DeviceDisconnected(disconnected) =>
                                if disconnected == id.platform { callbacks.disconnected() }
                            _ => {}
                        },
                    }
                }
            });
        });

        peripheral
    }

    async fn properties(&self) -> Result<PeripheralProperties> {
        let platform = self.get_platform().await?.properties().await?.unwrap();
        Ok(PeripheralProperties::new(self.id.clone(), platform))
    }

    async fn connect(&self, cancellation_handle: Arc<CancellationHandle>) -> bool {
        if let Ok(platform) = self.get_platform().await {
            return self
                .platform_connect(platform, cancellation_handle)
                .await
                .is_ok();
        }

        // Ooph. We're in a bad place because btleplug removes peripherals from the adapter when
        // they disconnect. This means that we have to re-scan until the device shows back up in
        // the adapter and then attempt connection.
        let adapter = get_adapter().await;
        let mut events = match adapter.events().await {
            Ok(events) => events,
            Err(_) => return false,
        };
        if adapter.start_scan(ScanFilter::default()).await.is_err() {
            return false;
        }

        let peripheral_token = self.cancellation.token();
        let connect_token = cancellation_handle.token();
        loop {
            tokio::select! {
                _ = peripheral_token.cancelled() => break,
                _ = connect_token.cancelled() => break,
                Some(event) = events.next() => match event {
                    CentralEvent::DeviceConnected(_) | CentralEvent::DeviceUpdated(_) => {
                        if adapter.peripheral(&self.id.platform).await.is_ok() { break; }
                    },
                    _ => {}
                }
            }
        }

        if let Ok(platform) = self.get_platform().await {
            self.platform_connect(platform, cancellation_handle)
                .await
                .is_ok()
        } else {
            false
        }
    }

    async fn disconnect(&self) -> bool {
        match self.get_platform().await {
            Err(_) => true,
            Ok(platform) => platform.disconnect().await.is_ok(),
        }
    }

    async fn discover_services(&self) -> Result<()> {
        self.get_platform()
            .await?
            .discover_services()
            .await
            .map_err(Into::into)
    }

    async fn services(&self) -> Result<Vec<Service>> {
        self.get_platform()
            .await
            .map(|p| p.services().into_iter().map(Into::into).collect())
    }

    async fn read(&self, characteristic: Characteristic) -> Result<Vec<u8>> {
        self.get_platform()
            .await?
            .read(&characteristic.into())
            .await
            .map_err(Into::into)
    }

    async fn write(
        &self,
        characteristic: Characteristic,
        data: Vec<u8>,
        write_type: WriteType,
    ) -> Result<()> {
        self.get_platform()
            .await?
            .write(&characteristic.into(), &data, write_type.into())
            .await
            .map_err(Into::into)
    }

    async fn read_descriptor(&self, descriptor: Descriptor) -> Result<Vec<u8>> {
        self.get_platform()
            .await?
            .read_descriptor(&descriptor.into())
            .await
            .map_err(Into::into)
    }

    async fn write_descriptor(&self, descriptor: Descriptor, data: Vec<u8>) -> Result<()> {
        self.get_platform()
            .await?
            .write_descriptor(&descriptor.into(), &data)
            .await
            .map_err(Into::into)
    }

    async fn subscribe(&self, characteristic: Characteristic) -> Result<()> {
        self.get_platform()
            .await?
            .subscribe(&characteristic.into())
            .await
            .map_err(Into::into)
    }

    async fn unsubscribe(&self, characteristic: Characteristic) -> Result<()> {
        self.get_platform()
            .await?
            .unsubscribe(&characteristic.into())
            .await
            .map_err(Into::into)
    }
}

impl Drop for Peripheral {
    fn drop(&mut self) {
        self.cancellation.cancel();
    }
}
