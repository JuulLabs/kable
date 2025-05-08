use crate::uuid::Uuid;
use std::collections::HashMap;
use std::sync::Arc;

/// ID is a UUID on Apple, a path on linux, and a mac-address on Windows.
#[derive(Clone, uniffi::Record)]
pub struct PeripheralProperties {
    pub id: Arc<crate::peripheral_id::PeripheralId>,
    pub local_name: Option<String>,
    pub tx_power_level: Option<i16>,
    pub rssi: Option<i16>,
    pub manufacturer_data: HashMap<u16, Vec<u8>>,
    pub service_data: HashMap<Uuid, Vec<u8>>,
    pub services: Vec<Uuid>,
    pub class: Option<u32>,
}

impl PeripheralProperties {
    pub fn new(
        id: Arc<crate::peripheral_id::PeripheralId>,
        platform: btleplug::api::PeripheralProperties,
    ) -> Self {
        Self {
            id,
            local_name: platform.local_name,
            tx_power_level: platform.tx_power_level,
            rssi: platform.rssi,
            manufacturer_data: platform.manufacturer_data,
            service_data: platform
                .service_data
                .into_iter()
                .map(|(uuid, bytes)| (uuid.into(), bytes))
                .collect(),
            services: platform
                .services
                .into_iter()
                .map(|uuid| uuid.into())
                .collect(),
            class: platform.class,
        }
    }
}
