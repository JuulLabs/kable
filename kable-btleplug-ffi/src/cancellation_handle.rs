use tokio_util::sync::CancellationToken;

#[derive(Clone, uniffi::Object)]
pub struct CancellationHandle {
    cancellation_token: CancellationToken,
}

impl CancellationHandle {
    pub fn from_token(token: CancellationToken) -> Self {
        CancellationHandle {
            cancellation_token: token,
        }
    }

    pub fn token(&self) -> CancellationToken {
        self.cancellation_token.clone()
    }
}

#[uniffi::export]
impl CancellationHandle {
    #[uniffi::constructor]
    pub fn new() -> Self {
        Self::from_token(CancellationToken::new())
    }

    pub fn cancel(&self) {
        self.cancellation_token.cancel();
    }
}
