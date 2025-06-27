#[derive(Copy, Clone, Debug, uniffi::Enum)]
pub enum WriteType {
    WithResponse,
    WithoutResponse,
}

impl From<WriteType> for btleplug::api::WriteType {
    fn from(value: WriteType) -> Self {
        match value {
            WriteType::WithResponse => btleplug::api::WriteType::WithResponse,
            WriteType::WithoutResponse => btleplug::api::WriteType::WithoutResponse,
        }
    }
}

impl From<btleplug::api::WriteType> for WriteType {
    fn from(value: btleplug::api::WriteType) -> Self {
        match value {
            btleplug::api::WriteType::WithResponse => WriteType::WithResponse,
            btleplug::api::WriteType::WithoutResponse => WriteType::WithoutResponse,
        }
    }
}
