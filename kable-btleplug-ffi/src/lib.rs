use btleplug::{api::Manager as _, platform::Manager};
use tokio::sync::OnceCell;

uniffi::setup_scaffolding!();

pub mod cancellation_handle;
pub mod peripheral_id;
pub mod scan;
pub mod uuid;

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
