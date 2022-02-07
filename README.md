![badge][badge-android]
![badge][badge-ios]
![badge][badge-js]
![badge][badge-mac]
[![Slack](https://img.shields.io/badge/Slack-%23juul--libraries-ECB22E.svg?logo=data:image/svg+xml;base64,PHN2ZyB2aWV3Qm94PSIwIDAgNTQgNTQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGcgZmlsbD0ibm9uZSIgZmlsbC1ydWxlPSJldmVub2RkIj48cGF0aCBkPSJNMTkuNzEyLjEzM2E1LjM4MSA1LjM4MSAwIDAgMC01LjM3NiA1LjM4NyA1LjM4MSA1LjM4MSAwIDAgMCA1LjM3NiA1LjM4Nmg1LjM3NlY1LjUyQTUuMzgxIDUuMzgxIDAgMCAwIDE5LjcxMi4xMzNtMCAxNC4zNjVINS4zNzZBNS4zODEgNS4zODEgMCAwIDAgMCAxOS44ODRhNS4zODEgNS4zODEgMCAwIDAgNS4zNzYgNS4zODdoMTQuMzM2YTUuMzgxIDUuMzgxIDAgMCAwIDUuMzc2LTUuMzg3IDUuMzgxIDUuMzgxIDAgMCAwLTUuMzc2LTUuMzg2IiBmaWxsPSIjMzZDNUYwIi8+PHBhdGggZD0iTTUzLjc2IDE5Ljg4NGE1LjM4MSA1LjM4MSAwIDAgMC01LjM3Ni01LjM4NiA1LjM4MSA1LjM4MSAwIDAgMC01LjM3NiA1LjM4NnY1LjM4N2g1LjM3NmE1LjM4MSA1LjM4MSAwIDAgMCA1LjM3Ni01LjM4N20tMTQuMzM2IDBWNS41MkE1LjM4MSA1LjM4MSAwIDAgMCAzNC4wNDguMTMzYTUuMzgxIDUuMzgxIDAgMCAwLTUuMzc2IDUuMzg3djE0LjM2NGE1LjM4MSA1LjM4MSAwIDAgMCA1LjM3NiA1LjM4NyA1LjM4MSA1LjM4MSAwIDAgMCA1LjM3Ni01LjM4NyIgZmlsbD0iIzJFQjY3RCIvPjxwYXRoIGQ9Ik0zNC4wNDggNTRhNS4zODEgNS4zODEgMCAwIDAgNS4zNzYtNS4zODcgNS4zODEgNS4zODEgMCAwIDAtNS4zNzYtNS4zODZoLTUuMzc2djUuMzg2QTUuMzgxIDUuMzgxIDAgMCAwIDM0LjA0OCA1NG0wLTE0LjM2NWgxNC4zMzZhNS4zODEgNS4zODEgMCAwIDAgNS4zNzYtNS4zODYgNS4zODEgNS4zODEgMCAwIDAtNS4zNzYtNS4zODdIMzQuMDQ4YTUuMzgxIDUuMzgxIDAgMCAwLTUuMzc2IDUuMzg3IDUuMzgxIDUuMzgxIDAgMCAwIDUuMzc2IDUuMzg2IiBmaWxsPSIjRUNCMjJFIi8+PHBhdGggZD0iTTAgMzQuMjQ5YTUuMzgxIDUuMzgxIDAgMCAwIDUuMzc2IDUuMzg2IDUuMzgxIDUuMzgxIDAgMCAwIDUuMzc2LTUuMzg2di01LjM4N0g1LjM3NkE1LjM4MSA1LjM4MSAwIDAgMCAwIDM0LjI1bTE0LjMzNi0uMDAxdjE0LjM2NEE1LjM4MSA1LjM4MSAwIDAgMCAxOS43MTIgNTRhNS4zODEgNS4zODEgMCAwIDAgNS4zNzYtNS4zODdWMzQuMjVhNS4zODEgNS4zODEgMCAwIDAtNS4zNzYtNS4zODcgNS4zODEgNS4zODEgMCAwIDAtNS4zNzYgNS4zODciIGZpbGw9IiNFMDFFNUEiLz48L2c+PC9zdmc+&labelColor=611f69)](https://kotlinlang.slack.com/messages/juul-libraries/)

# Kable

**K**otlin **A**synchronous **B**luetooth **L**ow **E**nergy provides a simple Coroutines-powered API for interacting
with Bluetooth Low Energy devices.

Usage is demonstrated with the [SensorTag sample app].

## Scanning

To scan for nearby peripherals, the [`Scanner`] provides an [`advertisements`] [`Flow`] which is a stream of
[`Advertisement`] objects representing advertisements seen from nearby peripherals. [`Advertisement`] objects contain
information such as the peripheral's name and RSSI (signal strength).

The [`Scanner`] may be configured via the following DSL (shown are defaults, when not specified):

```kotlin
val scanner = Scanner {
    filters = null
    logging {
        engine = SystemLogEngine
        level = Warnings
        format = Multiline
    }
}
```

To filter scan results at the system level (recommended), specify a list of filters for the services the remote
peripheral is advertising, for example:

```kotlin
val scanner = Scanner {
    filters = listOf(
        Filter.Service(uuidFrom("f000aa80-0451-4000-b000-000000000000")),
        Filter.Service(uuidFrom("f000aa81-0451-4000-b000-000000000000"))
    )
}
```

In Android source sets, you can also scan with manufacturer data filters. See the Android section below for more details.

Scanning begins when the [`advertisements`] [`Flow`] is collected and stops when the [`Flow`] collection is terminated.
A [`Flow`] terminal operator (such as [`first`]) may be used to scan until an advertisement is found that matches a
desired predicate. 

```kotlin
val advertisement = Scanner()
    .advertisements
    .first { it.name?.startsWith("Example") }
```

### Android

Scan results can be filtered by manufacturer data using the same ID, data, and data mask that you would use with the
[Android API](https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder#setManufacturerData(int,%20byte[],%20byte[])):

```kotlin
val scanner = Scanner {
    filters = listOf(
        Filter.ManufacturerData(id = 1, data = byteArrayOf(), dataMask = byteArrayOf())
    )
}
``` 

Android also offers additional settings to customize scanning. They are available via the `scanSettings` property in the
[`Scanner`] builder DSL. Simply set `scanSettings` property to an Android [`ScanSettings`] object, for example:

```kotlin
val scanner = Scanner {
    scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
}
```

_The `scanSettings` property is only available on Android and is considered a Kable obsolete API, meaning it will be
removed when a DSL specific API becomes available._

### JavaScript

_Scanning for nearby peripherals is supported, but only available on Chrome 79+ with "Experimental Web
Platform features" enabled via:_ `chrome://flags/#enable-experimental-web-platform-features`

## Peripheral

Once an [`Advertisement`] is obtained, it can be converted to a [`Peripheral`] via the [`CoroutineScope.peripheral`]
extension function. [`Peripheral`] objects represent actions that can be performed against a remote peripheral, such as
connection handling and I/O operations.

```kotlin
val peripheral = scope.peripheral(advertisement)
```

### Configuration

To configure a `peripheral`, options may be set in the builder lambda:

```kotlin
val peripheral = scope.peripheral(advertisement) {
    // Set peripheral configuration.
}
```

#### Logging

By default, Kable only logs a small number of warnings when unexpected failures occur. To aid in debugging, additional
logging may be enabled and configured via the `logging` DSL, for example:

```kotlin
val peripheral = scope.peripheral(advertisement) {
    logging {
        level = Events // or Data
    }
}
```

The available log levels are:

- `Warnings`: Logs warnings when unexpected failures occur _(default)_
- `Events`: Same as `Warnings` plus logs all events (e.g. writing to a characteristic)
- `Data`: Same as `Events` plus string representation of I/O data

Available logging settings are as follows (all settings are optional; shown are defaults, when not specified):

```kotlin
val peripheral = scope.peripheral(advertisement) {
    logging {
        engine = SystemLogEngine
        level = Warnings
        format = Multiline
        data = Hex
    }
}
```

The format of the logs can be either `Compact` (on a single line per log) or `Multiline` (spanning multiple lines for
details):

| `Compact` | `Multiline` _(default)_ |
|-----------|-------------------------|
| <pre>example message(detail1=value1, detail2=value2, ...)</pre> | <pre>example message<br/>  detail1: value1<br/>  detail2: value2<br/>  ...</pre> |

Display format of I/O data may be customized, either by configuring the `Hex` representation, or by providing a
`DataProcessor`, for example:

```kotlin
val peripheral = scope.peripheral(advertisement) {
    logging {
        data = Hex {
            separator = " "
            lowerCase = false
        }

        // or...

        data = DataProcessor { bytes ->
            // todo: Convert `bytes` to desired String representation, for example:
            bytes.joinToString { byte -> byte.toString() } // Show data as integer representation of bytes.
        }
    }
}
```

_I/O data is only shown in logs when logging `level` is set to `Data`._

When logging, the identity of the peripheral is prefixed on log messages to differentiate messages when multiple
peripherals are logging. The identifier (for the purposes of logging) can be set via the `identifier` property:

```kotlin
val peripheral = scope.peripheral(advertisement) {
    logging {
        identifier = "Example"
    }
}
```

The default (when not specified, or set to `null`) is to use the platform specific peripheral identifier:

- Android: Hardware (MAC) address (e.g. "00:11:22:AA:BB:CC")
- Apple: The UUID associated with the peer
- JavaScript: A `DOMString` that uniquely identifies a device

#### Service Discovery

All platforms support an `onServicesDiscovered` action (that is executed after service discovery but before observations
are wired up):

```kotlin
val peripheral = scope.peripheral(advertisement) {
    onServicesDiscovered {
        // Perform any desired I/O operations.
    }
}
```

_Exceptions thrown in `onServicesDiscovered` are propagated to the `Peripheral`'s [`connect`] call._

### Android

On Android targets, additional configuration options are available (all configuration directives are optional):

```kotlin
val peripheral = scope.peripheral(advertisement) {
    onServicesDiscovered {
        requestMtu(...)
    }
    transport = Transport.Le // default
    phy = Phy.Le1M // default
}
```

### JavaScript

On JavaScript, rather than processing a stream of advertisements, a specific peripheral can be requested using the
[`CoroutineScope.requestPeripheral`] extension function. Criteria ([`Options`]) such as expected service UUIDs on the
peripheral and/or the peripheral's name may be specified. When [`requestPeripheral`] is called with the specified
options, the browser shows the user a list of peripherals matching the criteria. The peripheral chosen by the user is
then returned (as a [`Peripheral`] object).

```kotlin
val options = Options(
    optionalServices = arrayOf(
        "f000aa80-0451-4000-b000-000000000000",
        "f000aa81-0451-4000-b000-000000000000"
    ),
    filters = arrayOf(
        NamePrefix("Example")
    )
)
val peripheral = scope.requestPeripheral(options).await()
```

## Connectivity

Once a [`Peripheral`] object is acquired, a connection can be established via the [`connect`] function. The [`connect`]
method suspends until a connection is established and ready (or a failure occurs). A connection is considered ready when
connected, services have been discovered, and observations (if any) have been re-wired. _Service discovery occurs
automatically upon connection._

_Multiple concurrent calls to [`connect`] will all suspend until connection is ready._

```kotlin
peripheral.connect()
```

To disconnect, the [`disconnect`] function will disconnect an active connection, or cancel an in-flight connection
attempt. The [`disconnect`] function suspends until the peripheral has settled on a disconnected state.

```kotlin
peripheral.disconnect()
```

_If the underlying subsystem fails to deliver the disconnected state then the [`disconnect`] call could potentially
stall indefinitely. To prevent this (and ensure underlying resources are cleaned up in a timely manner) it is
recommended that [`disconnect`] be wrapped with a timeout, for example:_

```kotlin
// Allow 5 seconds for graceful disconnect before forcefully closing `Peripheral`.
withTimeoutOrNull(5_000L) {
    peripheral.disconnect()
}
```

#### State

The connection state of a [`Peripheral`] can be monitored via its [`state`] [`Flow`].

```kotlin
peripheral.state.collect { state ->
    // Display and/or process the connection state.
}
```

The [`state`] will typically transition through the following [`State`][connection-state]s:

![Connection states](artwork/connection-states.png)

_[`Disconnecting`] state only occurs on Android platform. JavaScript and Apple-based platforms transition directly from
[`Connected`] to [`Disconnected`] (upon calling [`disconnect`] function, or when a connection is dropped)._

### I/O

Bluetooth Low Energy devices are organized into a tree-like structure of services, characteristics and descriptors;
whereas characteristics and descriptors have the capability of being read from, or written to.

For example, a peripheral might have the following structure:

- Service S1 (`00001815-0000-1000-8000-00805f9b34fb`)
    - Characteristic C1
        - Descriptor D1
        - Descriptor D2
    - Characteristic C2 (`00002a56-0000-1000-8000-00805f9b34fb`)
        - Descriptor D3 (`00002902-0000-1000-8000-00805f9b34fb`)
- Service S2
    - Characteristic C3

To access a characteristic or descriptor, use the [`charactisticOf`] or [`descriptorOf`] functions, respectively. These
functions lazily search for the first match (based on UUID) in the GATT profile when performing I/O.

_When performing I/O operations on a characteristic ([`read`], [`write`], [`observe`]), the properties of the
characteristic are taken into account when finding the first match. For example, when performing a [`write`] with a
[`WriteType`] of [`WithResponse`], the first characteristic matching the expected UUID **and** having the
[`writeWithResponse`] property will be used._

In the above example, to lazily access "Descriptor D3":

```kotlin
val descriptor = descriptorOf(
    service = "00001815-0000-1000-8000-00805f9b34fb",
    characteristic = "00002a56-0000-1000-8000-00805f9b34fb",
    descriptor = "00002902-0000-1000-8000-00805f9b34fb"
)
```

Alternatively, a characteristic or descriptor may be obtained by traversing the [`Peripheral.services`]. This is useful
when multiple characteristics or descriptors have the same UUID. Objects obtained from the [`Peripheral.services`] hold
strong references to the underlying platform types, so special care must be taken to properly remove references to
objects retrieved from [`Peripheral.services`] when no longer needed.

To access "Descriptor D3" using a discovered descriptor:

```kotlin
val services = peripheral.services ?: error("Services have not been discovered")
val descriptor = services
    .first { it.serviceUuid == uuidFrom("00001815-0000-1000-8000-00805f9b34fb") }
    .characteristics
    .first { it.characteristicUuid == uuidFrom("00002a56-0000-1000-8000-00805f9b34fb") }
    .descriptors
    .first { it.descriptorUuid == uuidFrom("00002902-0000-1000-8000-00805f9b34fb") }
```

_This example uses a similar search algorithm as `descriptorOf`, but other search methods may be utilized. For example,
properties of the characteristic could be queried to find a specific characteristic that is expected to be the parent of
the sought after descriptor. When searching for a specific characteristic, descriptors can be read that may identity the
sought after characteristic._

When connected, data can be read from, or written to, characteristics and/or descriptors via [`read`] and [`write`]
functions.

_The [`read`] and [`write`] functions throw [`NotReadyException`] until a connection is established._

```kotlin
val data = peripheral.read(characteristic)

peripheral.write(descriptor, byteArrayOf(1, 2, 3))
```

### Observation

Bluetooth Low Energy provides the capability of subscribing to characteristic changes by means of notifications and/or
indications, whereas a characteristic change on a connected peripheral is "pushed" to the central via a characteristic
notification and/or indication which carries the new value of the characteristic.

Characteristic change notifications/indications can be observed/subscribed to via the [`observe`] function which returns
a [`Flow`] of the new characteristic data.

```kotlin
val observation = peripheral.observe(characteristic)
observation.collect { data ->
    // Process data.
}
```

When used with [`characteristicOf`], the [`observe`] function can be called (and its returned [`Flow`] can be collected)
prior to a connection being established. Once a connection is established then characteristic changes will stream from
the [`Flow`]. If the connection drops, the [`Flow`] will remain active, and upon reconnecting it will resume streaming
characteristic changes.

A [`Characteristic`] may also be obtained via the [`Peripheral.services`] property and used with the [`observe`]
function. As before, if the connection drops, the [`Flow`] will remain active, upon reconnecting the same underlying
platform characteristic will be used to to resume streaming characteristic changes.

Failures related to notifications/indications are propagated via the [`observe`] [`Flow`], for example, if the
associated characteristic is invalid or cannot be found, then a `NoSuchElementException` is propagated via the
[`observe`] [`Flow`]. An [`ObservationExceptionHandler`] may be registered with the [`Peripheral`] to control which
failures are propagated through (and terminate) the [`observe`] [`Flow`], for example:

```kotlin
scope.peripheral(advertisement) {
    observationExceptionHandler { cause ->
        // Log failure instead of propagating associated `observe` flow.
        println("Observation failure suppressed: $cause")
    }
}
```

In scenarios where an I/O operation needs to be performed upon subscribing to the [`observe`] [`Flow`], an `onSubscribe`
action may be specified:

```kotlin
val observation = peripheral.observe(characteristic) {
    // Perform desired I/O operations upon collecting from the `observe` Flow, for example:
    peripheral.write(descriptor, "ping".toByteArray())
}
observation.collect { data ->
    // Process data.
}
```

In the above example, `"ping"` will be written to the `descriptor` when:

- [Connection][`connect`] is established (while the returned [`Flow`] is active); and
- _After_ the observation is spun up (i.e. after enabling notifications or indications)

The `onSubscription` action is useful in situations where an initial operation is needed when starting an observation
(such as writing a configuration to the peripheral and expecting the response to come back in the form of a
characteristic change).

## Structured Concurrency

Peripheral objects/connections are scoped to a [Coroutine scope]. When creating a [`Peripheral`], the
[`CoroutineScope.peripheral`] extension function is used, which scopes the returned [`Peripheral`] to the
[`CoroutineScope`] receiver. If the [`CoroutineScope`] receiver is cancelled then the [`Peripheral`] will disconnect and
be disposed.

```kotlin
Scanner()
    .advertisements
    .filter { advertisement -> advertisement.name?.startsWith("Example") }
    .map { advertisement -> scope.peripheral(advertisement) }
    .onEach { peripheral -> peripheral.connect() }
    .launchIn(scope)

delay(60_000L)
scope.cancel() // All `peripherals` will implicitly disconnect and be disposed.
```

_[`Peripheral.disconnect`] is the preferred method of disconnecting peripherals, but disposal via Coroutine scope
cancellation is provided to prevent connection leaks._

## Setup

### Gradle

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.juul.kable/core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.juul.kable/core)

Kable can be configured via Gradle Kotlin DSL as follows:

#### Multiplatform

```kotlin
plugins {
    id("com.android.application") // or id("com.android.library")
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    android()
    js().browser()
    macosX64()
    iosX64()
    iosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
                implementation("com.juul.kable:core:${kableVersion}")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${coroutinesVersion}")
            }
        }

        val macosX64Main by getting {
            dependencies {
                // Need to specify the Coroutines artifact specific for the target platform (`-macosx64`):
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-macosx64:${coroutinesVersion}-native-mt") {
                    version {
                        // `strictly` needed to make sure Gradle uses `-native-mt` version.
                        strictly("${coroutinesVersion}-native-mt")
                    }
                }
            }
        }

        val iosX64Main by getting {
            dependencies {
                // Need to specify the Coroutines artifact specific for the target platform (`-iosx64`):
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-iosx64:${coroutinesVersion}-native-mt") {
                    version {
                        // `strictly` needed to make sure Gradle uses `-native-mt` version.
                        strictly("${coroutinesVersion}-native-mt")
                    }
                }
            }
        }

        val iosArm64Main by getting {
            dependencies {
                // Need to specify the Coroutines artifact specific for the target platform (`-iosarm64`):
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-iosarm64:${coroutinesVersion}-native-mt") {
                    version {
                        // `strictly` needed to make sure Gradle uses `-native-mt` version.
                        strictly("${coroutinesVersion}-native-mt")
                    }
                }
            }
        }
    }
}

android {
    // ...
}
```

_Note that for compatibility with Kable, Native targets (e.g. `macosX64`) require
[Coroutines with multithread support for Kotlin/Native] (more specifically: Coroutines library artifacts that are
suffixed with `-native-mt`)._

#### Platform-specific

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.juul.kable:core:$version")
}
```
# License

```
Copyright 2020 JUUL Labs, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


[SensorTag sample app]: https://github.com/JuulLabs/sensortag
[Coroutines with multithread support for Kotlin/Native]: https://github.com/Kotlin/kotlinx.coroutines/issues/462
[Coroutine scope]: https://kotlinlang.org/docs/reference/coroutines/coroutine-context-and-dispatchers.html#coroutine-scope
[`ScanSettings`]: https://developer.android.com/reference/kotlin/android/bluetooth/le/ScanSettings

[`Advertisement`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-advertisement/index.html
[`Characteristic`]: https://juullabs.github.io/kable/core/com.juul.kable/-characteristic/index.html
[`Connected`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-state/index.html#%5Bcom.juul.kable%2FState.Connected%2F%2F%2FPointingToDeclaration%2F%5D%2FClasslikes%2F-328684452
[`CoroutineScope.peripheral`]: https://juullabs.github.io/kable/core/core/com.juul.kable/peripheral.html
[`CoroutineScope.requestPeripheral`]: https://juullabs.github.io/kable/core/core/com.juul.kable/request-peripheral.html
[`CoroutineScope`]: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/
[`Disconnected`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-state/index.html#%5Bcom.juul.kable%2FState.Disconnected%2F%2F%2FPointingToDeclaration%2F%5D%2FClasslikes%2F-328684452
[`Disconnecting`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-state/index.html#%5Bcom.juul.kable%2FState.Disconnecting%2F%2F%2FPointingToDeclaration%2F%5D%2FClasslikes%2F-328684452
[`Flow`]: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/
[`NotReadyException`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-not-ready-exception/index.html
[`Options`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-options/index.html
[`Peripheral.disconnect`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fdisconnect%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-328684452
[`Peripheral.services`]: https://juullabs.github.io/kable/core/com.juul.kable/-peripheral/index.html#-1607712299%2FProperties%2F-2011752812
[`Peripheral`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html
[`Scanner`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-scanner/index.html
[`WithResponse`]: https://juullabs.github.io/kable/core/com.juul.kable/-write-type/index.html#-1405019860%2FClasslikes%2F-2011752812
[`WriteType`]: https://juullabs.github.io/kable/core/com.juul.kable/-write-type/index.html
[`advertisements`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-scanner/index.html#%5Bcom.juul.kable%2FScanner%2Fadvertisements%2F%23%2FPointingToDeclaration%2F%5D%2FProperties%2F-328684452
[`charactisticOf`]: https://juullabs.github.io/kable/core/core/com.juul.kable/characteristic-of.html
[`connect`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fconnect%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-328684452
[`descriptorOf`]: https://juullabs.github.io/kable/core/core/com.juul.kable/descriptor-of.html
[`disconnect`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fdisconnect%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-328684452
[`first`]: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/first.html
[`observe`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fobserve%2F%23com.juul.kable.Characteristic%2FPointingToDeclaration%2F%5D%2FFunctions%2F-328684452
[`read`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fread%2F%23com.juul.kable.Characteristic%2FPointingToDeclaration%2F%2C+com.juul.kable%2FPeripheral%2Fread%2F%23com.juul.kable.Descriptor%2FPointingToDeclaration%2F%5D%2FFunctions%2F-328684452
[`requestPeripheral`]: https://juullabs.github.io/kable/core/core/com.juul.kable/request-peripheral.html
[`state`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fstate%2F%23%2FPointingToDeclaration%2F%5D%2FProperties%2F-328684452
[`writeWithResponse`]: https://juullabs.github.io/kable/core/com.juul.kable/-characteristic/-properties/index.html#491699083%2FExtensions%2F-2011752812
[`write`]: https://juullabs.github.io/kable/core/core/com.juul.kable/-peripheral/index.html#%5Bcom.juul.kable%2FPeripheral%2Fwrite%2F%23com.juul.kable.Descriptor%23kotlin.ByteArray%2FPointingToDeclaration%2F%2C+com.juul.kable%2FPeripheral%2Fwrite%2F%23com.juul.kable.Characteristic%23kotlin.ByteArray%23com.juul.kable.WriteType%2FPointingToDeclaration%2F%5D%2FFunctions%2F-328684452
[connection-state]: https://juullabs.github.io/kable/core/core/com.juul.kable/-state/index.html

[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat
[badge-windows]: http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat
[badge-mac]: http://img.shields.io/badge/platform-macos-111111.svg?style=flat
[badge-watchos]: http://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat
[badge-tvos]: http://img.shields.io/badge/platform-tvos-808080.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/platform-wasm-624FE8.svg?style=flat
