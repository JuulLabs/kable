use btleplug::{
    api::{Central, CentralEvent, Manager as _, ScanFilter},
    platform::Manager,
};
use tokio::sync::OnceCell;
use tokio_stream::StreamExt;
use tokio_util::sync::CancellationToken;

static MANAGER: OnceCell<Manager> = OnceCell::const_new();

async fn get_adapter() -> btleplug::platform::Adapter {
    MANAGER
        .get_or_init(async || Manager::new().await.unwrap())
        .await
        .adapters()
        .await
        .unwrap()
        .remove(0)
}

uniffi::setup_scaffolding!();

#[derive(uniffi::Object)]
pub struct CancellationHandle {
    cancellation_token: CancellationToken,
}

impl CancellationHandle {
    pub fn cancel(&self) {
        self.cancellation_token.cancel();
    }
}

#[uniffi::export(callback_interface)]
#[async_trait::async_trait]
pub trait ScanCallback: Send + Sync {
    async fn on_advertisement(&self, name: String);
}

#[uniffi::export(async_runtime = "tokio")]
pub async fn scan(callbacks: Box<dyn ScanCallback>) -> CancellationHandle {
    let token = CancellationToken::new();
    let handle = CancellationHandle {
        cancellation_token: token.clone(),
    };

    let adapter = get_adapter().await;
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
                        CentralEvent::ServicesAdvertisement { id, services: _ } => {
                            callbacks.on_advertisement(id.to_string()).await;
                        }
                        _ => {}
                    }
                }
            }
        });
    });
    handle
}
