use tokio_util::sync::CancellationToken;

#[derive(Clone, uniffi::Object)]
pub struct CancellationHandle {
    cancellation_token: CancellationToken,
}

impl CancellationHandle {
    #[uniffi::constructor]
    pub fn new(token: CancellationToken) -> Self {
        CancellationHandle {
            cancellation_token: token,
        }
    }

    pub fn cancel(&self) {
        self.cancellation_token.cancel();
    }
}
