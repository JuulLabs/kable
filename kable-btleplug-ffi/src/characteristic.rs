use crate::descriptor::Descriptor;
use crate::uuid::Uuid;
use btleplug::api::CharPropFlags;

#[derive(Clone, uniffi::Record)]
pub struct Characteristic {
    pub uuid: Uuid,
    pub service: Uuid,
    pub properties: CharacteristicPropertyFlags,
    pub descriptors: Vec<Descriptor>,
}

#[derive(Clone, uniffi::Record)]
pub struct CharacteristicPropertyFlags {
    bits: u8,
}

impl From<Characteristic> for btleplug::api::Characteristic {
    fn from(value: Characteristic) -> Self {
        btleplug::api::Characteristic {
            uuid: value.uuid.into(),
            service_uuid: value.service.into(),
            properties: value.properties.into(),
            descriptors: value.descriptors.into_iter().map(|x| x.into()).collect(),
        }
    }
}

impl From<btleplug::api::Characteristic> for Characteristic {
    fn from(value: btleplug::api::Characteristic) -> Self {
        Characteristic {
            uuid: value.uuid.into(),
            service: value.service_uuid.into(),
            properties: value.properties.into(),
            descriptors: value.descriptors.into_iter().map(|x| x.into()).collect(),
        }
    }
}

impl From<CharacteristicPropertyFlags> for CharPropFlags {
    fn from(value: CharacteristicPropertyFlags) -> Self {
        CharPropFlags::from_bits(value.bits).unwrap()
    }
}

impl From<CharPropFlags> for CharacteristicPropertyFlags {
    fn from(value: CharPropFlags) -> Self {
        CharacteristicPropertyFlags { bits: value.bits() }
    }
}
