uniffi::setup_scaffolding!();

#[uniffi::export(async_runtime = "tokio")]
pub async fn hello_world() -> String {
    "Hello, world!".to_owned()
}
