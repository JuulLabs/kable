use serde::{Deserialize, Serialize};
use std::fmt::Display;

#[derive(Clone, Deserialize, Serialize, uniffi::Object)]
#[uniffi::export(Display)]
pub struct PeripheralId {
    platform: btleplug::platform::PeripheralId,
}

// TODO: Identifier stingifying as JSON only really needs to happen on Linux.
//       Apple could be implemented directly as a Uuid/string, and Windows could use
//       the mac-address string.

#[uniffi::export]
impl PeripheralId {
    #[uniffi::constructor]
    fn new(json: String) -> Self {
        PeripheralId {
            platform: serde_json::from_str(&json).unwrap(),
        }
    }
}

impl Display for PeripheralId {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", serde_json::to_string(&self.platform).unwrap())
    }
}

impl From<btleplug::platform::PeripheralId> for PeripheralId {
    fn from(platform: btleplug::platform::PeripheralId) -> Self {
        Self { platform }
    }
}
