use crate::uuid::Uuid;

#[derive(Clone, uniffi::Record)]
pub struct Descriptor {
    pub uuid: Uuid,
    pub service: Uuid,
    pub characteristic: Uuid,
}

impl From<Descriptor> for btleplug::api::Descriptor {
    fn from(descriptor: Descriptor) -> Self {
        Self {
            uuid: descriptor.uuid.into(),
            service_uuid: descriptor.service.into(),
            characteristic_uuid: descriptor.characteristic.into(),
        }
    }
}

impl From<btleplug::api::Descriptor> for Descriptor {
    fn from(descriptor: btleplug::api::Descriptor) -> Self {
        Self {
            uuid: descriptor.uuid.into(),
            service: descriptor.service_uuid.into(),
            characteristic: descriptor.characteristic_uuid.into(),
        }
    }
}
