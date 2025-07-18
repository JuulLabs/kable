#[cfg(target_os = "windows")]
use btleplug::api::BDAddr;
#[cfg(target_os = "linux")]
use serde::{Deserialize, Serialize};
use std::fmt::Display;
#[cfg(target_os = "windows")]
use std::str::FromStr;

#[derive(Eq, PartialEq, Hash, Clone, uniffi::Object)]
#[uniffi::export(Display, Hash, Eq)]
pub struct PeripheralId {
    pub platform: btleplug::platform::PeripheralId,
}

#[cfg(target_os = "macos")]
#[uniffi::export]
impl PeripheralId {
    #[uniffi::constructor]
    fn new(value: String) -> Self {
        Self {
            platform: uuid::Uuid::parse_str(&value).unwrap().into(),
        }
    }
}

#[cfg(target_os = "macos")]
impl Display for PeripheralId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.platform)
    }
}

#[cfg(target_os = "linux")]
#[uniffi::export]
impl PeripheralId {
    #[uniffi::constructor]
    fn new(value: String) -> Self {
        PeripheralId {
            platform: serde_json::from_str(&value).unwrap(),
        }
    }
}

#[cfg(target_os = "linux")]
impl Display for PeripheralId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", serde_json::to_string(&self.platform).unwrap())
    }
}

#[cfg(target_os = "windows")]
#[uniffi::export]
impl PeripheralId {
    #[uniffi::constructor]
    fn new(value: String) -> Self {
        PeripheralId {
            platform: BDAddr::from_str(&value).unwrap().into(),
        }
    }
}

#[cfg(target_os = "windows")]
impl Display for PeripheralId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.platform.to_string())
    }
}

impl From<btleplug::platform::PeripheralId> for PeripheralId {
    fn from(platform: btleplug::platform::PeripheralId) -> Self {
        Self { platform }
    }
}
