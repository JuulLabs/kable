#[derive(Clone, Eq, PartialEq, Hash)]
pub struct Uuid(String);

impl From<Uuid> for uuid::Uuid {
    fn from(value: Uuid) -> Self {
        uuid::Uuid::parse_str(&value.0).unwrap()
    }
}

impl From<uuid::Uuid> for Uuid {
    fn from(value: uuid::Uuid) -> Self {
        Uuid(value.to_string())
    }
}

uniffi::custom_type!(Uuid, String, {
    lower: |uuid| uuid.0,
    try_lift: |s| Ok(Uuid(s)),
});
