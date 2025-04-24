uniffi::setup_scaffolding!();

#[uniffi::export(async_runtime = "tokio")]
pub async fn hello_world() -> String {
    // Sleep is just used to verify that the async runtime is working.
    tokio::time::sleep(tokio::time::Duration::from_millis(1000)).await;
    "Hello, world!".to_owned()
}
