# GATT Server (Peripheral Role) Sample

Demonstrates using the `kable-server` module to host (and advertise) a
[Heart Rate service](https://www.bluetooth.com/specifications/specs/heart-rate-service-1-0/) GATT
server on Android:

- **Heart Rate Measurement** (`0x2A37`): notifies subscribed centrals of the (simulated, via
  slider) heart rate once per second.
- **Body Sensor Location** (`0x2A38`): static (read-only) value.

## Running

```
./gradlew :app:installDebug
```

## Verifying (with nRF Connect)

1. Launch the app, grant the requested Bluetooth permissions, then tap **Start** followed by
   **Advertise**.
2. On a second device, scan with [nRF Connect] (the peripheral advertises the Heart Rate service
   UUID `0x180D`).
3. Connect, then:
   - Read **Body Sensor Location** (`0x2A38`) — should show `0x01` (Chest).
   - Subscribe to **Heart Rate Measurement** (`0x2A37`) — notifications should arrive once per
     second, and the app's *Subscribers* count should increment.
   - Move the slider in the app — notified values should change accordingly.
4. Disconnect — the app's *Subscribers* count should decrement.

[nRF Connect]: https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp
