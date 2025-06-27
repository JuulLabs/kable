use btleplug::api::{Central, CentralState};
use btleplug::platform::Adapter;
use btleplug::{api::Manager as _, platform::Manager};
use tokio::sync::OnceCell;

uniffi::setup_scaffolding!();

pub mod cancellation_handle;
pub mod characteristic;
pub mod descriptor;
pub mod error;
pub mod peripheral;
pub mod peripheral_id;
pub mod peripheral_properties;
pub mod scan;
pub mod service;
pub mod uuid;
pub mod write_type;

pub use error::Error;
pub type Result<T> = std::result::Result<T, Error>;

static ADAPTER: OnceCell<Adapter> = OnceCell::const_new();

async fn get_adapter() -> Adapter {
    ADAPTER.get_or_init(create_adapter).await.clone()
}

async fn create_adapter() -> Adapter {
    Manager::new()
        .await
        .unwrap()
        .adapters()
        .await
        .unwrap()
        .get(0)
        .unwrap()
        .clone()
}

#[uniffi::export(async_runtime = "tokio")]
async fn is_adapter_on() -> bool {
    get_adapter()
        .await
        .adapter_state()
        .await
        .map_or_else(|_| false, |state| state == CentralState::PoweredOn)
}

#[uniffi::export(async_runtime = "tokio")]
async fn is_supported() -> bool {
    is_supported_result().await.is_ok()
}

async fn is_supported_result() -> Result<bool> {
    Manager::new()
        .await?
        .adapters()
        .await?
        .get(0)
        .map(|_| true)
        .ok_or(Error::NotSupported("Unable to get adapter".to_string()))
}
