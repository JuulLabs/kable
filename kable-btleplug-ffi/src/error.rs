use std::time::Duration;

#[derive(Debug, thiserror::Error, uniffi::Enum)]
pub enum Error {
    #[error("Cancelled")]
    Cancelled,

    #[error("Permission denied")]
    PermissionDenied,

    #[error("Device not found")]
    DeviceNotFound,

    #[error("Not connected")]
    NotConnected,

    #[error("Unexpected callback")]
    UnexpectedCallback,

    #[error("Unexpected characteristic")]
    UnexpectedCharacteristic,

    #[error("No such characteristic")]
    NoSuchCharacteristic,

    #[error("The operation is not supported: {}", _0)]
    NotSupported(String),

    #[error("Timed out after {:?}", _0)]
    TimedOut(Duration),

    #[error("Error parsing UUID: {0}")]
    Uuid(String),

    #[error("Invalid Bluetooth address: {0}")]
    InvalidBDAddr(String),

    #[error("Runtime Error: {}", _0)]
    RuntimeError(String),

    #[error("Other Error: {}", _0)]
    Other(String),
}

impl From<btleplug::Error> for Error {
    fn from(value: btleplug::Error) -> Self {
        match value {
            btleplug::Error::PermissionDenied => Self::PermissionDenied,
            btleplug::Error::DeviceNotFound => Self::DeviceNotFound,
            btleplug::Error::NotConnected => Self::NotConnected,
            btleplug::Error::UnexpectedCallback => Self::UnexpectedCallback,
            btleplug::Error::UnexpectedCharacteristic => Self::UnexpectedCharacteristic,
            btleplug::Error::NoSuchCharacteristic => Self::NoSuchCharacteristic,
            btleplug::Error::NotSupported(s) => Self::NotSupported(s),
            btleplug::Error::TimedOut(d) => Self::TimedOut(d),
            btleplug::Error::Uuid(e) => Self::Uuid(e.to_string()),
            btleplug::Error::InvalidBDAddr(e) => Self::InvalidBDAddr(e.to_string()),
            btleplug::Error::RuntimeError(s) => Self::RuntimeError(s),
            _ => Self::Other(value.to_string()),
        }
    }
}
