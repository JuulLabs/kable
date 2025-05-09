use crate::characteristic::Characteristic;
use crate::uuid::Uuid;

#[derive(Clone, uniffi::Record)]
pub struct Service {
    pub uuid: Uuid,
    pub primary: bool,
    pub characteristics: Vec<Characteristic>,
}

impl From<Service> for btleplug::api::Service {
    fn from(value: Service) -> Self {
        btleplug::api::Service {
            uuid: value.uuid.into(),
            primary: value.primary,
            characteristics: value
                .characteristics
                .into_iter()
                .map(|x| x.into())
                .collect(),
        }
    }
}

impl From<btleplug::api::Service> for Service {
    fn from(value: btleplug::api::Service) -> Self {
        Service {
            uuid: value.uuid.into(),
            primary: value.primary,
            characteristics: value
                .characteristics
                .into_iter()
                .map(|x| x.into())
                .collect(),
        }
    }
}
