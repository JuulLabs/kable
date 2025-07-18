#[derive(Copy, Clone, Debug, uniffi::Enum)]
pub enum WriteType {
    WithResponse,
    WithoutResponse,
}

impl From<WriteType> for btleplug::api::WriteType {
    fn from(value: WriteType) -> Self {
        match value {
            WriteType::WithResponse => Self::WithResponse,
            WriteType::WithoutResponse => Self::WithoutResponse,
        }
    }
}

impl From<btleplug::api::WriteType> for WriteType {
    fn from(value: btleplug::api::WriteType) -> Self {
        match value {
            btleplug::api::WriteType::WithResponse => Self::WithResponse,
            btleplug::api::WriteType::WithoutResponse => Self::WithoutResponse,
        }
    }
}
