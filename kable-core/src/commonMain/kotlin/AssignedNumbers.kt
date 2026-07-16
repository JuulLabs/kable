// Bluetooth SIG assigned numbers sourced from:
// - https://github.com/chungchungdev/kotlin-bluetoothsig-assigned-numbers/blob/master/generated/AssignedNumbers.kt @ 2616a854ccf80fa8034dccebbef206b9d99fad40
//   Copyright chungchungdev, used under the Apache License, Version 2.0.

package com.juul.kable

/**
 * Bluetooth SIG assigned numbers, as Kotlin constants.
 *
 * Combine with [Bluetooth.BaseUuid] to produce a full 128-bit [Uuid][kotlin.uuid.Uuid], for
 * example:
 *
 * ```
 * val heartRateServiceUuid = Bluetooth.BaseUuid + AssignedNumbers.Services.HEART_RATE
 * println(heartRateServiceUuid) // Output: 0000180d-0000-1000-8000-00805f9b34fb
 * ```
 *
 * https://www.bluetooth.com/specifications/assigned-numbers/
 *
 * Last update: 2025-05-05 (UTC+0)
 */
@ExperimentalKableApi
public object AssignedNumbers {
    /**
     * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/uuids/service_uuids.yaml
     */
    public object Services {
        /**
         * GAP
         *
         * 0x1800
         *
         * org.bluetooth.service.gap
         */
        public const val GAP: UInt = 0x1800_u

        /**
         * GATT
         *
         * 0x1801
         *
         * org.bluetooth.service.gatt
         */
        public const val GATT: UInt = 0x1801_u

        /**
         * Immediate Alert
         *
         * 0x1802
         *
         * org.bluetooth.service.immediate_alert
         */
        public const val IMMEDIATE_ALERT: UInt = 0x1802_u

        /**
         * Link Loss
         *
         * 0x1803
         *
         * org.bluetooth.service.link_loss
         */
        public const val LINK_LOSS: UInt = 0x1803_u

        /**
         * Tx Power
         *
         * 0x1804
         *
         * org.bluetooth.service.tx_power
         */
        public const val TX_POWER: UInt = 0x1804_u

        /**
         * Current Time
         *
         * 0x1805
         *
         * org.bluetooth.service.current_time
         */
        public const val CURRENT_TIME: UInt = 0x1805_u

        /**
         * Reference Time Update
         *
         * 0x1806
         *
         * org.bluetooth.service.reference_time_update
         */
        public const val REFERENCE_TIME_UPDATE: UInt = 0x1806_u

        /**
         * Next DST Change
         *
         * 0x1807
         *
         * org.bluetooth.service.next_dst_change
         */
        public const val NEXT_DST_CHANGE: UInt = 0x1807_u

        /**
         * Glucose
         *
         * 0x1808
         *
         * org.bluetooth.service.glucose
         */
        public const val GLUCOSE: UInt = 0x1808_u

        /**
         * Health Thermometer
         *
         * 0x1809
         *
         * org.bluetooth.service.health_thermometer
         */
        public const val HEALTH_THERMOMETER: UInt = 0x1809_u

        /**
         * Device Information
         *
         * 0x180A
         *
         * org.bluetooth.service.device_information
         */
        public const val DEVICE_INFORMATION: UInt = 0x180A_u

        /**
         * Heart Rate
         *
         * 0x180D
         *
         * org.bluetooth.service.heart_rate
         */
        public const val HEART_RATE: UInt = 0x180D_u

        /**
         * Phone Alert Status
         *
         * 0x180E
         *
         * org.bluetooth.service.phone_alert_status
         */
        public const val PHONE_ALERT_STATUS: UInt = 0x180E_u

        /**
         * Battery
         *
         * 0x180F
         *
         * org.bluetooth.service.battery_service
         */
        public const val BATTERY: UInt = 0x180F_u

        /**
         * Blood Pressure
         *
         * 0x1810
         *
         * org.bluetooth.service.blood_pressure
         */
        public const val BLOOD_PRESSURE: UInt = 0x1810_u

        /**
         * Alert Notification
         *
         * 0x1811
         *
         * org.bluetooth.service.alert_notification
         */
        public const val ALERT_NOTIFICATION: UInt = 0x1811_u

        /**
         * Human Interface Device
         *
         * 0x1812
         *
         * org.bluetooth.service.human_interface_device
         */
        public const val HUMAN_INTERFACE_DEVICE: UInt = 0x1812_u

        /**
         * Scan Parameters
         *
         * 0x1813
         *
         * org.bluetooth.service.scan_parameters
         */
        public const val SCAN_PARAMETERS: UInt = 0x1813_u

        /**
         * Running Speed and Cadence
         *
         * 0x1814
         *
         * org.bluetooth.service.running_speed_and_cadence
         */
        public const val RUNNING_SPEED_AND_CADENCE: UInt = 0x1814_u

        /**
         * Automation IO
         *
         * 0x1815
         *
         * org.bluetooth.service.automation_io
         */
        public const val AUTOMATION_IO: UInt = 0x1815_u

        /**
         * Cycling Speed and Cadence
         *
         * 0x1816
         *
         * org.bluetooth.service.cycling_speed_and_cadence
         */
        public const val CYCLING_SPEED_AND_CADENCE: UInt = 0x1816_u

        /**
         * Cycling Power
         *
         * 0x1818
         *
         * org.bluetooth.service.cycling_power
         */
        public const val CYCLING_POWER: UInt = 0x1818_u

        /**
         * Location and Navigation
         *
         * 0x1819
         *
         * org.bluetooth.service.location_and_navigation
         */
        public const val LOCATION_AND_NAVIGATION: UInt = 0x1819_u

        /**
         * Environmental Sensing
         *
         * 0x181A
         *
         * org.bluetooth.service.environmental_sensing
         */
        public const val ENVIRONMENTAL_SENSING: UInt = 0x181A_u

        /**
         * Body Composition
         *
         * 0x181B
         *
         * org.bluetooth.service.body_composition
         */
        public const val BODY_COMPOSITION: UInt = 0x181B_u

        /**
         * User Data
         *
         * 0x181C
         *
         * org.bluetooth.service.user_data
         */
        public const val USER_DATA: UInt = 0x181C_u

        /**
         * Weight Scale
         *
         * 0x181D
         *
         * org.bluetooth.service.weight_scale
         */
        public const val WEIGHT_SCALE: UInt = 0x181D_u

        /**
         * Bond Management
         *
         * 0x181E
         *
         * org.bluetooth.service.bond_management
         */
        public const val BOND_MANAGEMENT: UInt = 0x181E_u

        /**
         * Continuous Glucose Monitoring
         *
         * 0x181F
         *
         * org.bluetooth.service.continuous_glucose_monitoring
         */
        public const val CONTINUOUS_GLUCOSE_MONITORING: UInt = 0x181F_u

        /**
         * Internet Protocol Support
         *
         * 0x1820
         *
         * org.bluetooth.service.internet_protocol_support
         */
        public const val INTERNET_PROTOCOL_SUPPORT: UInt = 0x1820_u

        /**
         * Indoor Positioning
         *
         * 0x1821
         *
         * org.bluetooth.service.indoor_positioning
         */
        public const val INDOOR_POSITIONING: UInt = 0x1821_u

        /**
         * Pulse Oximeter
         *
         * 0x1822
         *
         * org.bluetooth.service.pulse_oximeter
         */
        public const val PULSE_OXIMETER: UInt = 0x1822_u

        /**
         * HTTP Proxy
         *
         * 0x1823
         *
         * org.bluetooth.service.http_proxy
         */
        public const val HTTP_PROXY: UInt = 0x1823_u

        /**
         * Transport Discovery
         *
         * 0x1824
         *
         * org.bluetooth.service.transport_discovery
         */
        public const val TRANSPORT_DISCOVERY: UInt = 0x1824_u

        /**
         * Object Transfer
         *
         * 0x1825
         *
         * org.bluetooth.service.object_transfer
         */
        public const val OBJECT_TRANSFER: UInt = 0x1825_u

        /**
         * Fitness Machine
         *
         * 0x1826
         *
         * org.bluetooth.service.fitness_machine
         */
        public const val FITNESS_MACHINE: UInt = 0x1826_u

        /**
         * Mesh Provisioning
         *
         * 0x1827
         *
         * org.bluetooth.service.mesh_provisioning
         */
        public const val MESH_PROVISIONING: UInt = 0x1827_u

        /**
         * Mesh Proxy
         *
         * 0x1828
         *
         * org.bluetooth.service.mesh_proxy
         */
        public const val MESH_PROXY: UInt = 0x1828_u

        /**
         * Reconnection Configuration
         *
         * 0x1829
         *
         * org.bluetooth.service.reconnection_configuration
         */
        public const val RECONNECTION_CONFIGURATION: UInt = 0x1829_u

        /**
         * Insulin Delivery
         *
         * 0x183A
         *
         * org.bluetooth.service.insulin_delivery
         */
        public const val INSULIN_DELIVERY: UInt = 0x183A_u

        /**
         * Binary Sensor
         *
         * 0x183B
         *
         * org.bluetooth.service.binary_sensor
         */
        public const val BINARY_SENSOR: UInt = 0x183B_u

        /**
         * Emergency Configuration
         *
         * 0x183C
         *
         * org.bluetooth.service.emergency_configuration
         */
        public const val EMERGENCY_CONFIGURATION: UInt = 0x183C_u

        /**
         * Authorization Control
         *
         * 0x183D
         *
         * org.bluetooth.service.authorization_control
         */
        public const val AUTHORIZATION_CONTROL: UInt = 0x183D_u

        /**
         * Physical Activity Monitor
         *
         * 0x183E
         *
         * org.bluetooth.service.physical_activity_monitor
         */
        public const val PHYSICAL_ACTIVITY_MONITOR: UInt = 0x183E_u

        /**
         * Elapsed Time
         *
         * 0x183F
         *
         * org.bluetooth.service.elapsed_time
         */
        public const val ELAPSED_TIME: UInt = 0x183F_u

        /**
         * Generic Health Sensor
         *
         * 0x1840
         *
         * org.bluetooth.service.generic_health_sensor
         */
        public const val GENERIC_HEALTH_SENSOR: UInt = 0x1840_u

        /**
         * Audio Input Control
         *
         * 0x1843
         *
         * org.bluetooth.service.audio_input_control
         */
        public const val AUDIO_INPUT_CONTROL: UInt = 0x1843_u

        /**
         * Volume Control
         *
         * 0x1844
         *
         * org.bluetooth.service.volume_control
         */
        public const val VOLUME_CONTROL: UInt = 0x1844_u

        /**
         * Volume Offset Control
         *
         * 0x1845
         *
         * org.bluetooth.service.volume_offset
         */
        public const val VOLUME_OFFSET_CONTROL: UInt = 0x1845_u

        /**
         * Coordinated Set Identification
         *
         * 0x1846
         *
         * org.bluetooth.service.coordinated_set_identification
         */
        public const val COORDINATED_SET_IDENTIFICATION: UInt = 0x1846_u

        /**
         * Device Time
         *
         * 0x1847
         *
         * org.bluetooth.service.device_time
         */
        public const val DEVICE_TIME: UInt = 0x1847_u

        /**
         * Media Control
         *
         * 0x1848
         *
         * org.bluetooth.service.media_control
         */
        public const val MEDIA_CONTROL: UInt = 0x1848_u

        /**
         * Generic Media Control
         *
         * 0x1849
         *
         * org.bluetooth.service.generic_media_control
         */
        public const val GENERIC_MEDIA_CONTROL: UInt = 0x1849_u

        /**
         * Constant Tone Extension
         *
         * 0x184A
         *
         * org.bluetooth.service.constant_tone_extension
         */
        public const val CONSTANT_TONE_EXTENSION: UInt = 0x184A_u

        /**
         * Telephone Bearer
         *
         * 0x184B
         *
         * org.bluetooth.service.telephone_bearer
         */
        public const val TELEPHONE_BEARER: UInt = 0x184B_u

        /**
         * Generic Telephone Bearer
         *
         * 0x184C
         *
         * org.bluetooth.service.generic_telephone_bearer
         */
        public const val GENERIC_TELEPHONE_BEARER: UInt = 0x184C_u

        /**
         * Microphone Control
         *
         * 0x184D
         *
         * org.bluetooth.service.microphone_control
         */
        public const val MICROPHONE_CONTROL: UInt = 0x184D_u

        /**
         * Audio Stream Control
         *
         * 0x184E
         *
         * org.bluetooth.service.audio_stream_control
         */
        public const val AUDIO_STREAM_CONTROL: UInt = 0x184E_u

        /**
         * Broadcast Audio Scan
         *
         * 0x184F
         *
         * org.bluetooth.service.broadcast_audio_scan
         */
        public const val BROADCAST_AUDIO_SCAN: UInt = 0x184F_u

        /**
         * Published Audio Capabilities
         *
         * 0x1850
         *
         * org.bluetooth.service.published_audio_capabilities
         */
        public const val PUBLISHED_AUDIO_CAPABILITIES: UInt = 0x1850_u

        /**
         * Basic Audio Announcement
         *
         * 0x1851
         *
         * org.bluetooth.service.basic_audio_announcement
         */
        public const val BASIC_AUDIO_ANNOUNCEMENT: UInt = 0x1851_u

        /**
         * Broadcast Audio Announcement
         *
         * 0x1852
         *
         * org.bluetooth.service.broadcast_audio_announcement
         */
        public const val BROADCAST_AUDIO_ANNOUNCEMENT: UInt = 0x1852_u

        /**
         * Common Audio
         *
         * 0x1853
         *
         * org.bluetooth.service.common_audio
         */
        public const val COMMON_AUDIO: UInt = 0x1853_u

        /**
         * Hearing Access
         *
         * 0x1854
         *
         * org.bluetooth.service.hearing_access
         */
        public const val HEARING_ACCESS: UInt = 0x1854_u

        /**
         * Telephony and Media Audio
         *
         * 0x1855
         *
         * org.bluetooth.service.telephony_and_media_audio
         */
        public const val TELEPHONY_AND_MEDIA_AUDIO: UInt = 0x1855_u

        /**
         * Public Broadcast Announcement
         *
         * 0x1856
         *
         * org.bluetooth.service.public_broadcast_announcement
         */
        public const val PUBLIC_BROADCAST_ANNOUNCEMENT: UInt = 0x1856_u

        /**
         * Electronic Shelf Label
         *
         * 0x1857
         *
         * org.bluetooth.service.electronic_shelf_label
         */
        public const val ELECTRONIC_SHELF_LABEL: UInt = 0x1857_u

        /**
         * Gaming Audio
         *
         * 0x1858
         *
         * org.bluetooth.service.gaming_audio
         */
        public const val GAMING_AUDIO: UInt = 0x1858_u

        /**
         * Mesh Proxy Solicitation
         *
         * 0x1859
         *
         * org.bluetooth.service.mesh_proxy_solicitation
         */
        public const val MESH_PROXY_SOLICITATION: UInt = 0x1859_u

        /**
         * Industrial Measurement Device
         *
         * 0x185A
         *
         * org.bluetooth.service.industrial_measurement_device
         */
        public const val INDUSTRIAL_MEASUREMENT_DEVICE: UInt = 0x185A_u

        /**
         * Ranging
         *
         * 0x185B
         *
         * org.bluetooth.service.ranging
         */
        public const val RANGING: UInt = 0x185B_u
    }

    /**
     * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/uuids/characteristic_uuids.yaml
     */
    public object Characteristics {
        /**
         * Device Name
         *
         * 0x2A00
         *
         * org.bluetooth.characteristic.gap.device_name
         */
        public const val DEVICE_NAME: UInt = 0x2A00_u

        /**
         * Appearance
         *
         * 0x2A01
         *
         * org.bluetooth.characteristic.gap.appearance
         */
        public const val APPEARANCE: UInt = 0x2A01_u

        /**
         * Peripheral Privacy Flag
         *
         * 0x2A02
         *
         * org.bluetooth.characteristic.gap.peripheral_privacy_flag
         */
        public const val PERIPHERAL_PRIVACY_FLAG: UInt = 0x2A02_u

        /**
         * Reconnection Address
         *
         * 0x2A03
         *
         * org.bluetooth.characteristic.gap.reconnection_address
         */
        public const val RECONNECTION_ADDRESS: UInt = 0x2A03_u

        /**
         * Peripheral Preferred Connection Parameters
         *
         * 0x2A04
         *
         * org.bluetooth.characteristic.gap.peripheral_preferred_connection_parameters
         */
        public const val PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS: UInt = 0x2A04_u

        /**
         * Service Changed
         *
         * 0x2A05
         *
         * org.bluetooth.characteristic.gatt.service_changed
         */
        public const val SERVICE_CHANGED: UInt = 0x2A05_u

        /**
         * Alert Level
         *
         * 0x2A06
         *
         * org.bluetooth.characteristic.alert_level
         */
        public const val ALERT_LEVEL: UInt = 0x2A06_u

        /**
         * Tx Power Level
         *
         * 0x2A07
         *
         * org.bluetooth.characteristic.tx_power_level
         */
        public const val TX_POWER_LEVEL: UInt = 0x2A07_u

        /**
         * Date Time
         *
         * 0x2A08
         *
         * org.bluetooth.characteristic.date_time
         */
        public const val DATE_TIME: UInt = 0x2A08_u

        /**
         * Day of Week
         *
         * 0x2A09
         *
         * org.bluetooth.characteristic.day_of_week
         */
        public const val DAY_OF_WEEK: UInt = 0x2A09_u

        /**
         * Day Date Time
         *
         * 0x2A0A
         *
         * org.bluetooth.characteristic.day_date_time
         */
        public const val DAY_DATE_TIME: UInt = 0x2A0A_u

        /**
         * Exact Time 256
         *
         * 0x2A0C
         *
         * org.bluetooth.characteristic.exact_time_256
         */
        public const val EXACT_TIME_256: UInt = 0x2A0C_u

        /**
         * DST Offset
         *
         * 0x2A0D
         *
         * org.bluetooth.characteristic.dst_offset
         */
        public const val DST_OFFSET: UInt = 0x2A0D_u

        /**
         * Time Zone
         *
         * 0x2A0E
         *
         * org.bluetooth.characteristic.time_zone
         */
        public const val TIME_ZONE: UInt = 0x2A0E_u

        /**
         * Local Time Information
         *
         * 0x2A0F
         *
         * org.bluetooth.characteristic.local_time_information
         */
        public const val LOCAL_TIME_INFORMATION: UInt = 0x2A0F_u

        /**
         * Time with DST
         *
         * 0x2A11
         *
         * org.bluetooth.characteristic.time_with_dst
         */
        public const val TIME_WITH_DST: UInt = 0x2A11_u

        /**
         * Time Accuracy
         *
         * 0x2A12
         *
         * org.bluetooth.characteristic.time_accuracy
         */
        public const val TIME_ACCURACY: UInt = 0x2A12_u

        /**
         * Time Source
         *
         * 0x2A13
         *
         * org.bluetooth.characteristic.time_source
         */
        public const val TIME_SOURCE: UInt = 0x2A13_u

        /**
         * Reference Time Information
         *
         * 0x2A14
         *
         * org.bluetooth.characteristic.reference_time_information
         */
        public const val REFERENCE_TIME_INFORMATION: UInt = 0x2A14_u

        /**
         * Time Update Control Point
         *
         * 0x2A16
         *
         * org.bluetooth.characteristic.time_update_control_point
         */
        public const val TIME_UPDATE_CONTROL_POINT: UInt = 0x2A16_u

        /**
         * Time Update State
         *
         * 0x2A17
         *
         * org.bluetooth.characteristic.time_update_state
         */
        public const val TIME_UPDATE_STATE: UInt = 0x2A17_u

        /**
         * Glucose Measurement
         *
         * 0x2A18
         *
         * org.bluetooth.characteristic.glucose_measurement
         */
        public const val GLUCOSE_MEASUREMENT: UInt = 0x2A18_u

        /**
         * Battery Level
         *
         * 0x2A19
         *
         * org.bluetooth.characteristic.battery_level
         */
        public const val BATTERY_LEVEL: UInt = 0x2A19_u

        /**
         * Temperature Measurement
         *
         * 0x2A1C
         *
         * org.bluetooth.characteristic.temperature_measurement
         */
        public const val TEMPERATURE_MEASUREMENT: UInt = 0x2A1C_u

        /**
         * Temperature Type
         *
         * 0x2A1D
         *
         * org.bluetooth.characteristic.temperature_type
         */
        public const val TEMPERATURE_TYPE: UInt = 0x2A1D_u

        /**
         * Intermediate Temperature
         *
         * 0x2A1E
         *
         * org.bluetooth.characteristic.intermediate_temperature
         */
        public const val INTERMEDIATE_TEMPERATURE: UInt = 0x2A1E_u

        /**
         * Measurement Interval
         *
         * 0x2A21
         *
         * org.bluetooth.characteristic.measurement_interval
         */
        public const val MEASUREMENT_INTERVAL: UInt = 0x2A21_u

        /**
         * Boot Keyboard Input Report
         *
         * 0x2A22
         *
         * org.bluetooth.characteristic.boot_keyboard_input_report
         */
        public const val BOOT_KEYBOARD_INPUT_REPORT: UInt = 0x2A22_u

        /**
         * System ID
         *
         * 0x2A23
         *
         * org.bluetooth.characteristic.system_id
         */
        public const val SYSTEM_ID: UInt = 0x2A23_u

        /**
         * Model Number String
         *
         * 0x2A24
         *
         * org.bluetooth.characteristic.model_number_string
         */
        public const val MODEL_NUMBER_STRING: UInt = 0x2A24_u

        /**
         * Serial Number String
         *
         * 0x2A25
         *
         * org.bluetooth.characteristic.serial_number_string
         */
        public const val SERIAL_NUMBER_STRING: UInt = 0x2A25_u

        /**
         * Firmware Revision String
         *
         * 0x2A26
         *
         * org.bluetooth.characteristic.firmware_revision_string
         */
        public const val FIRMWARE_REVISION_STRING: UInt = 0x2A26_u

        /**
         * Hardware Revision String
         *
         * 0x2A27
         *
         * org.bluetooth.characteristic.hardware_revision_string
         */
        public const val HARDWARE_REVISION_STRING: UInt = 0x2A27_u

        /**
         * Software Revision String
         *
         * 0x2A28
         *
         * org.bluetooth.characteristic.software_revision_string
         */
        public const val SOFTWARE_REVISION_STRING: UInt = 0x2A28_u

        /**
         * Manufacturer Name String
         *
         * 0x2A29
         *
         * org.bluetooth.characteristic.manufacturer_name_string
         */
        public const val MANUFACTURER_NAME_STRING: UInt = 0x2A29_u

        /**
         * IEEE 11073-20601 Regulatory Certification Data List
         *
         * 0x2A2A
         *
         * org.bluetooth.characteristic.ieee_11073-20601_regulatory_certification_data_list
         */
        public const val IEEE_11073_20601_REGULATORY_CERTIFICATION_DATA_LIST: UInt = 0x2A2A_u

        /**
         * Current Time
         *
         * 0x2A2B
         *
         * org.bluetooth.characteristic.current_time
         */
        public const val CURRENT_TIME: UInt = 0x2A2B_u

        /**
         * Magnetic Declination
         *
         * 0x2A2C
         *
         * org.bluetooth.characteristic.magnetic_declination
         */
        public const val MAGNETIC_DECLINATION: UInt = 0x2A2C_u

        /**
         * Scan Refresh
         *
         * 0x2A31
         *
         * org.bluetooth.characteristic.scan_refresh
         */
        public const val SCAN_REFRESH: UInt = 0x2A31_u

        /**
         * Boot Keyboard Output Report
         *
         * 0x2A32
         *
         * org.bluetooth.characteristic.boot_keyboard_output_report
         */
        public const val BOOT_KEYBOARD_OUTPUT_REPORT: UInt = 0x2A32_u

        /**
         * Boot Mouse Input Report
         *
         * 0x2A33
         *
         * org.bluetooth.characteristic.boot_mouse_input_report
         */
        public const val BOOT_MOUSE_INPUT_REPORT: UInt = 0x2A33_u

        /**
         * Glucose Measurement Context
         *
         * 0x2A34
         *
         * org.bluetooth.characteristic.glucose_measurement_context
         */
        public const val GLUCOSE_MEASUREMENT_CONTEXT: UInt = 0x2A34_u

        /**
         * Blood Pressure Measurement
         *
         * 0x2A35
         *
         * org.bluetooth.characteristic.blood_pressure_measurement
         */
        public const val BLOOD_PRESSURE_MEASUREMENT: UInt = 0x2A35_u

        /**
         * Intermediate Cuff Pressure
         *
         * 0x2A36
         *
         * org.bluetooth.characteristic.intermediate_cuff_pressure
         */
        public const val INTERMEDIATE_CUFF_PRESSURE: UInt = 0x2A36_u

        /**
         * Heart Rate Measurement
         *
         * 0x2A37
         *
         * org.bluetooth.characteristic.heart_rate_measurement
         */
        public const val HEART_RATE_MEASUREMENT: UInt = 0x2A37_u

        /**
         * Body Sensor Location
         *
         * 0x2A38
         *
         * org.bluetooth.characteristic.body_sensor_location
         */
        public const val BODY_SENSOR_LOCATION: UInt = 0x2A38_u

        /**
         * Heart Rate Control Point
         *
         * 0x2A39
         *
         * org.bluetooth.characteristic.heart_rate_control_point
         */
        public const val HEART_RATE_CONTROL_POINT: UInt = 0x2A39_u

        /**
         * Alert Status
         *
         * 0x2A3F
         *
         * org.bluetooth.characteristic.alert_status
         */
        public const val ALERT_STATUS: UInt = 0x2A3F_u

        /**
         * Ringer Control Point
         *
         * 0x2A40
         *
         * org.bluetooth.characteristic.ringer_control_point
         */
        public const val RINGER_CONTROL_POINT: UInt = 0x2A40_u

        /**
         * Ringer Setting
         *
         * 0x2A41
         *
         * org.bluetooth.characteristic.ringer_setting
         */
        public const val RINGER_SETTING: UInt = 0x2A41_u

        /**
         * Alert Category ID Bit Mask
         *
         * 0x2A42
         *
         * org.bluetooth.characteristic.alert_category_id_bit_mask
         */
        public const val ALERT_CATEGORY_ID_BIT_MASK: UInt = 0x2A42_u

        /**
         * Alert Category ID
         *
         * 0x2A43
         *
         * org.bluetooth.characteristic.alert_category_id
         */
        public const val ALERT_CATEGORY_ID: UInt = 0x2A43_u

        /**
         * Alert Notification Control Point
         *
         * 0x2A44
         *
         * org.bluetooth.characteristic.alert_notification_control_point
         */
        public const val ALERT_NOTIFICATION_CONTROL_POINT: UInt = 0x2A44_u

        /**
         * Unread Alert Status
         *
         * 0x2A45
         *
         * org.bluetooth.characteristic.unread_alert_status
         */
        public const val UNREAD_ALERT_STATUS: UInt = 0x2A45_u

        /**
         * New Alert
         *
         * 0x2A46
         *
         * org.bluetooth.characteristic.new_alert
         */
        public const val NEW_ALERT: UInt = 0x2A46_u

        /**
         * Supported New Alert Category
         *
         * 0x2A47
         *
         * org.bluetooth.characteristic.supported_new_alert_category
         */
        public const val SUPPORTED_NEW_ALERT_CATEGORY: UInt = 0x2A47_u

        /**
         * Supported Unread Alert Category
         *
         * 0x2A48
         *
         * org.bluetooth.characteristic.supported_unread_alert_category
         */
        public const val SUPPORTED_UNREAD_ALERT_CATEGORY: UInt = 0x2A48_u

        /**
         * Blood Pressure Feature
         *
         * 0x2A49
         *
         * org.bluetooth.characteristic.blood_pressure_feature
         */
        public const val BLOOD_PRESSURE_FEATURE: UInt = 0x2A49_u

        /**
         * HID Information
         *
         * 0x2A4A
         *
         * org.bluetooth.characteristic.hid_information
         */
        public const val HID_INFORMATION: UInt = 0x2A4A_u

        /**
         * Report Map
         *
         * 0x2A4B
         *
         * org.bluetooth.characteristic.report_map
         */
        public const val REPORT_MAP: UInt = 0x2A4B_u

        /**
         * HID Control Point
         *
         * 0x2A4C
         *
         * org.bluetooth.characteristic.hid_control_point
         */
        public const val HID_CONTROL_POINT: UInt = 0x2A4C_u

        /**
         * Report
         *
         * 0x2A4D
         *
         * org.bluetooth.characteristic.report
         */
        public const val REPORT: UInt = 0x2A4D_u

        /**
         * Protocol Mode
         *
         * 0x2A4E
         *
         * org.bluetooth.characteristic.protocol_mode
         */
        public const val PROTOCOL_MODE: UInt = 0x2A4E_u

        /**
         * Scan Interval Window
         *
         * 0x2A4F
         *
         * org.bluetooth.characteristic.scan_interval_window
         */
        public const val SCAN_INTERVAL_WINDOW: UInt = 0x2A4F_u

        /**
         * PnP ID
         *
         * 0x2A50
         *
         * org.bluetooth.characteristic.pnp_id
         */
        public const val PN_P_ID: UInt = 0x2A50_u

        /**
         * Glucose Feature
         *
         * 0x2A51
         *
         * org.bluetooth.characteristic.glucose_feature
         */
        public const val GLUCOSE_FEATURE: UInt = 0x2A51_u

        /**
         * Record Access Control Point
         *
         * 0x2A52
         *
         * org.bluetooth.characteristic.record_access_control_point
         */
        public const val RECORD_ACCESS_CONTROL_POINT: UInt = 0x2A52_u

        /**
         * RSC Measurement
         *
         * 0x2A53
         *
         * org.bluetooth.characteristic.rsc_measurement
         */
        public const val RSC_MEASUREMENT: UInt = 0x2A53_u

        /**
         * RSC Feature
         *
         * 0x2A54
         *
         * org.bluetooth.characteristic.rsc_feature
         */
        public const val RSC_FEATURE: UInt = 0x2A54_u

        /**
         * SC Control Point
         *
         * 0x2A55
         *
         * org.bluetooth.characteristic.sc_control_point
         */
        public const val SC_CONTROL_POINT: UInt = 0x2A55_u

        /**
         * Aggregate
         *
         * 0x2A5A
         *
         * org.bluetooth.characteristic.aggregate
         */
        public const val AGGREGATE: UInt = 0x2A5A_u

        /**
         * CSC Measurement
         *
         * 0x2A5B
         *
         * org.bluetooth.characteristic.csc_measurement
         */
        public const val CSC_MEASUREMENT: UInt = 0x2A5B_u

        /**
         * CSC Feature
         *
         * 0x2A5C
         *
         * org.bluetooth.characteristic.csc_feature
         */
        public const val CSC_FEATURE: UInt = 0x2A5C_u

        /**
         * Sensor Location
         *
         * 0x2A5D
         *
         * org.bluetooth.characteristic.sensor_location
         */
        public const val SENSOR_LOCATION: UInt = 0x2A5D_u

        /**
         * PLX Spot-Check Measurement
         *
         * 0x2A5E
         *
         * org.bluetooth.characteristic.plx_spot_check_measurement
         */
        public const val PLX_SPOT_CHECK_MEASUREMENT: UInt = 0x2A5E_u

        /**
         * PLX Continuous Measurement
         *
         * 0x2A5F
         *
         * org.bluetooth.characteristic.plx_continuous_measurement
         */
        public const val PLX_CONTINUOUS_MEASUREMENT: UInt = 0x2A5F_u

        /**
         * PLX Features
         *
         * 0x2A60
         *
         * org.bluetooth.characteristic.plx_features
         */
        public const val PLX_FEATURES: UInt = 0x2A60_u

        /**
         * Cycling Power Measurement
         *
         * 0x2A63
         *
         * org.bluetooth.characteristic.cycling_power_measurement
         */
        public const val CYCLING_POWER_MEASUREMENT: UInt = 0x2A63_u

        /**
         * Cycling Power Vector
         *
         * 0x2A64
         *
         * org.bluetooth.characteristic.cycling_power_vector
         */
        public const val CYCLING_POWER_VECTOR: UInt = 0x2A64_u

        /**
         * Cycling Power Feature
         *
         * 0x2A65
         *
         * org.bluetooth.characteristic.cycling_power_feature
         */
        public const val CYCLING_POWER_FEATURE: UInt = 0x2A65_u

        /**
         * Cycling Power Control Point
         *
         * 0x2A66
         *
         * org.bluetooth.characteristic.cycling_power_control_point
         */
        public const val CYCLING_POWER_CONTROL_POINT: UInt = 0x2A66_u

        /**
         * Location and Speed
         *
         * 0x2A67
         *
         * org.bluetooth.characteristic.location_and_speed
         */
        public const val LOCATION_AND_SPEED: UInt = 0x2A67_u

        /**
         * Navigation
         *
         * 0x2A68
         *
         * org.bluetooth.characteristic.navigation
         */
        public const val NAVIGATION: UInt = 0x2A68_u

        /**
         * Position Quality
         *
         * 0x2A69
         *
         * org.bluetooth.characteristic.position_quality
         */
        public const val POSITION_QUALITY: UInt = 0x2A69_u

        /**
         * LN Feature
         *
         * 0x2A6A
         *
         * org.bluetooth.characteristic.ln_feature
         */
        public const val LN_FEATURE: UInt = 0x2A6A_u

        /**
         * LN Control Point
         *
         * 0x2A6B
         *
         * org.bluetooth.characteristic.ln_control_point
         */
        public const val LN_CONTROL_POINT: UInt = 0x2A6B_u

        /**
         * Elevation
         *
         * 0x2A6C
         *
         * org.bluetooth.characteristic.elevation
         */
        public const val ELEVATION: UInt = 0x2A6C_u

        /**
         * Pressure
         *
         * 0x2A6D
         *
         * org.bluetooth.characteristic.pressure
         */
        public const val PRESSURE: UInt = 0x2A6D_u

        /**
         * Temperature
         *
         * 0x2A6E
         *
         * org.bluetooth.characteristic.temperature
         */
        public const val TEMPERATURE: UInt = 0x2A6E_u

        /**
         * Humidity
         *
         * 0x2A6F
         *
         * org.bluetooth.characteristic.humidity
         */
        public const val HUMIDITY: UInt = 0x2A6F_u

        /**
         * True Wind Speed
         *
         * 0x2A70
         *
         * org.bluetooth.characteristic.true_wind_speed
         */
        public const val TRUE_WIND_SPEED: UInt = 0x2A70_u

        /**
         * True Wind Direction
         *
         * 0x2A71
         *
         * org.bluetooth.characteristic.true_wind_direction
         */
        public const val TRUE_WIND_DIRECTION: UInt = 0x2A71_u

        /**
         * Apparent Wind Speed
         *
         * 0x2A72
         *
         * org.bluetooth.characteristic.apparent_wind_speed
         */
        public const val APPARENT_WIND_SPEED: UInt = 0x2A72_u

        /**
         * Apparent Wind Direction
         *
         * 0x2A73
         *
         * org.bluetooth.characteristic.apparent_wind_direction
         */
        public const val APPARENT_WIND_DIRECTION: UInt = 0x2A73_u

        /**
         * Gust Factor
         *
         * 0x2A74
         *
         * org.bluetooth.characteristic.gust_factor
         */
        public const val GUST_FACTOR: UInt = 0x2A74_u

        /**
         * Pollen Concentration
         *
         * 0x2A75
         *
         * org.bluetooth.characteristic.pollen_concentration
         */
        public const val POLLEN_CONCENTRATION: UInt = 0x2A75_u

        /**
         * UV Index
         *
         * 0x2A76
         *
         * org.bluetooth.characteristic.uv_index
         */
        public const val UV_INDEX: UInt = 0x2A76_u

        /**
         * Irradiance
         *
         * 0x2A77
         *
         * org.bluetooth.characteristic.irradiance
         */
        public const val IRRADIANCE: UInt = 0x2A77_u

        /**
         * Rainfall
         *
         * 0x2A78
         *
         * org.bluetooth.characteristic.rainfall
         */
        public const val RAINFALL: UInt = 0x2A78_u

        /**
         * Wind Chill
         *
         * 0x2A79
         *
         * org.bluetooth.characteristic.wind_chill
         */
        public const val WIND_CHILL: UInt = 0x2A79_u

        /**
         * Heat Index
         *
         * 0x2A7A
         *
         * org.bluetooth.characteristic.heat_index
         */
        public const val HEAT_INDEX: UInt = 0x2A7A_u

        /**
         * Dew Point
         *
         * 0x2A7B
         *
         * org.bluetooth.characteristic.dew_point
         */
        public const val DEW_POINT: UInt = 0x2A7B_u

        /**
         * Descriptor Value Changed
         *
         * 0x2A7D
         *
         * org.bluetooth.characteristic.descriptor_value_changed
         */
        public const val DESCRIPTOR_VALUE_CHANGED: UInt = 0x2A7D_u

        /**
         * Aerobic Heart Rate Lower Limit
         *
         * 0x2A7E
         *
         * org.bluetooth.characteristic.aerobic_heart_rate_lower_limit
         */
        public const val AEROBIC_HEART_RATE_LOWER_LIMIT: UInt = 0x2A7E_u

        /**
         * Aerobic Threshold
         *
         * 0x2A7F
         *
         * org.bluetooth.characteristic.aerobic_threshold
         */
        public const val AEROBIC_THRESHOLD: UInt = 0x2A7F_u

        /**
         * Age
         *
         * 0x2A80
         *
         * org.bluetooth.characteristic.age
         */
        public const val AGE: UInt = 0x2A80_u

        /**
         * Anaerobic Heart Rate Lower Limit
         *
         * 0x2A81
         *
         * org.bluetooth.characteristic.anaerobic_heart_rate_lower_limit
         */
        public const val ANAEROBIC_HEART_RATE_LOWER_LIMIT: UInt = 0x2A81_u

        /**
         * Anaerobic Heart Rate Upper Limit
         *
         * 0x2A82
         *
         * org.bluetooth.characteristic.anaerobic_heart_rate_upper_limit
         */
        public const val ANAEROBIC_HEART_RATE_UPPER_LIMIT: UInt = 0x2A82_u

        /**
         * Anaerobic Threshold
         *
         * 0x2A83
         *
         * org.bluetooth.characteristic.anaerobic_threshold
         */
        public const val ANAEROBIC_THRESHOLD: UInt = 0x2A83_u

        /**
         * Aerobic Heart Rate Upper Limit
         *
         * 0x2A84
         *
         * org.bluetooth.characteristic.aerobic_heart_rate_upper_limit
         */
        public const val AEROBIC_HEART_RATE_UPPER_LIMIT: UInt = 0x2A84_u

        /**
         * Date of Birth
         *
         * 0x2A85
         *
         * org.bluetooth.characteristic.date_of_birth
         */
        public const val DATE_OF_BIRTH: UInt = 0x2A85_u

        /**
         * Date of Threshold Assessment
         *
         * 0x2A86
         *
         * org.bluetooth.characteristic.date_of_threshold_assessment
         */
        public const val DATE_OF_THRESHOLD_ASSESSMENT: UInt = 0x2A86_u

        /**
         * Email Address
         *
         * 0x2A87
         *
         * org.bluetooth.characteristic.email_address
         */
        public const val EMAIL_ADDRESS: UInt = 0x2A87_u

        /**
         * Fat Burn Heart Rate Lower Limit
         *
         * 0x2A88
         *
         * org.bluetooth.characteristic.fat_burn_heart_rate_lower_limit
         */
        public const val FAT_BURN_HEART_RATE_LOWER_LIMIT: UInt = 0x2A88_u

        /**
         * Fat Burn Heart Rate Upper Limit
         *
         * 0x2A89
         *
         * org.bluetooth.characteristic.fat_burn_heart_rate_upper_limit
         */
        public const val FAT_BURN_HEART_RATE_UPPER_LIMIT: UInt = 0x2A89_u

        /**
         * First Name
         *
         * 0x2A8A
         *
         * org.bluetooth.characteristic.first_name
         */
        public const val FIRST_NAME: UInt = 0x2A8A_u

        /**
         * Five Zone Heart Rate Limits
         *
         * 0x2A8B
         *
         * org.bluetooth.characteristic.five_zone_heart_rate_limits
         */
        public const val FIVE_ZONE_HEART_RATE_LIMITS: UInt = 0x2A8B_u

        /**
         * Gender
         *
         * 0x2A8C
         *
         * org.bluetooth.characteristic.gender
         */
        public const val GENDER: UInt = 0x2A8C_u

        /**
         * Heart Rate Max
         *
         * 0x2A8D
         *
         * org.bluetooth.characteristic.heart_rate_max
         */
        public const val HEART_RATE_MAX: UInt = 0x2A8D_u

        /**
         * Height
         *
         * 0x2A8E
         *
         * org.bluetooth.characteristic.height
         */
        public const val HEIGHT: UInt = 0x2A8E_u

        /**
         * Hip Circumference
         *
         * 0x2A8F
         *
         * org.bluetooth.characteristic.hip_circumference
         */
        public const val HIP_CIRCUMFERENCE: UInt = 0x2A8F_u

        /**
         * Last Name
         *
         * 0x2A90
         *
         * org.bluetooth.characteristic.last_name
         */
        public const val LAST_NAME: UInt = 0x2A90_u

        /**
         * Maximum Recommended Heart Rate
         *
         * 0x2A91
         *
         * org.bluetooth.characteristic.maximum_recommended_heart_rate
         */
        public const val MAXIMUM_RECOMMENDED_HEART_RATE: UInt = 0x2A91_u

        /**
         * Resting Heart Rate
         *
         * 0x2A92
         *
         * org.bluetooth.characteristic.resting_heart_rate
         */
        public const val RESTING_HEART_RATE: UInt = 0x2A92_u

        /**
         * Sport Type for Aerobic and Anaerobic Thresholds
         *
         * 0x2A93
         *
         * org.bluetooth.characteristic.sport_type_for_aerobic_and_anaerobic_thresholds
         */
        public const val SPORT_TYPE_FOR_AEROBIC_AND_ANAEROBIC_THRESHOLDS: UInt = 0x2A93_u

        /**
         * Three Zone Heart Rate Limits
         *
         * 0x2A94
         *
         * org.bluetooth.characteristic.three_zone_heart_rate_limits
         */
        public const val THREE_ZONE_HEART_RATE_LIMITS: UInt = 0x2A94_u

        /**
         * Two Zone Heart Rate Limits
         *
         * 0x2A95
         *
         * org.bluetooth.characteristic.two_zone_heart_rate_limits
         */
        public const val TWO_ZONE_HEART_RATE_LIMITS: UInt = 0x2A95_u

        /**
         * VO2 Max
         *
         * 0x2A96
         *
         * org.bluetooth.characteristic.vo2_max
         */
        public const val VO2_MAX: UInt = 0x2A96_u

        /**
         * Waist Circumference
         *
         * 0x2A97
         *
         * org.bluetooth.characteristic.waist_circumference
         */
        public const val WAIST_CIRCUMFERENCE: UInt = 0x2A97_u

        /**
         * Weight
         *
         * 0x2A98
         *
         * org.bluetooth.characteristic.weight
         */
        public const val WEIGHT: UInt = 0x2A98_u

        /**
         * Database Change Increment
         *
         * 0x2A99
         *
         * org.bluetooth.characteristic.database_change_increment
         */
        public const val DATABASE_CHANGE_INCREMENT: UInt = 0x2A99_u

        /**
         * User Index
         *
         * 0x2A9A
         *
         * org.bluetooth.characteristic.user_index
         */
        public const val USER_INDEX: UInt = 0x2A9A_u

        /**
         * Body Composition Feature
         *
         * 0x2A9B
         *
         * org.bluetooth.characteristic.body_composition_feature
         */
        public const val BODY_COMPOSITION_FEATURE: UInt = 0x2A9B_u

        /**
         * Body Composition Measurement
         *
         * 0x2A9C
         *
         * org.bluetooth.characteristic.body_composition_measurement
         */
        public const val BODY_COMPOSITION_MEASUREMENT: UInt = 0x2A9C_u

        /**
         * Weight Measurement
         *
         * 0x2A9D
         *
         * org.bluetooth.characteristic.weight_measurement
         */
        public const val WEIGHT_MEASUREMENT: UInt = 0x2A9D_u

        /**
         * Weight Scale Feature
         *
         * 0x2A9E
         *
         * org.bluetooth.characteristic.weight_scale_feature
         */
        public const val WEIGHT_SCALE_FEATURE: UInt = 0x2A9E_u

        /**
         * User Control Point
         *
         * 0x2A9F
         *
         * org.bluetooth.characteristic.user_control_point
         */
        public const val USER_CONTROL_POINT: UInt = 0x2A9F_u

        /**
         * Magnetic Flux Density - 2D
         *
         * 0x2AA0
         *
         * org.bluetooth.characteristic.magnetic_flux_density_2d
         */
        public const val MAGNETIC_FLUX_DENSITY_2D: UInt = 0x2AA0_u

        /**
         * Magnetic Flux Density - 3D
         *
         * 0x2AA1
         *
         * org.bluetooth.characteristic.magnetic_flux_density_3d
         */
        public const val MAGNETIC_FLUX_DENSITY_3D: UInt = 0x2AA1_u

        /**
         * Language
         *
         * 0x2AA2
         *
         * org.bluetooth.characteristic.language
         */
        public const val LANGUAGE: UInt = 0x2AA2_u

        /**
         * Barometric Pressure Trend
         *
         * 0x2AA3
         *
         * org.bluetooth.characteristic.barometric_pressure_trend
         */
        public const val BAROMETRIC_PRESSURE_TREND: UInt = 0x2AA3_u

        /**
         * Bond Management Control Point
         *
         * 0x2AA4
         *
         * org.bluetooth.characteristic.bond_management_control_point
         */
        public const val BOND_MANAGEMENT_CONTROL_POINT: UInt = 0x2AA4_u

        /**
         * Bond Management Feature
         *
         * 0x2AA5
         *
         * org.bluetooth.characteristic.bond_management_feature
         */
        public const val BOND_MANAGEMENT_FEATURE: UInt = 0x2AA5_u

        /**
         * Central Address Resolution
         *
         * 0x2AA6
         *
         * org.bluetooth.characteristic.gap.central_address_resolution
         */
        public const val CENTRAL_ADDRESS_RESOLUTION: UInt = 0x2AA6_u

        /**
         * CGM Measurement
         *
         * 0x2AA7
         *
         * org.bluetooth.characteristic.cgm_measurement
         */
        public const val CGM_MEASUREMENT: UInt = 0x2AA7_u

        /**
         * CGM Feature
         *
         * 0x2AA8
         *
         * org.bluetooth.characteristic.cgm_feature
         */
        public const val CGM_FEATURE: UInt = 0x2AA8_u

        /**
         * CGM Status
         *
         * 0x2AA9
         *
         * org.bluetooth.characteristic.cgm_status
         */
        public const val CGM_STATUS: UInt = 0x2AA9_u

        /**
         * CGM Session Start Time
         *
         * 0x2AAA
         *
         * org.bluetooth.characteristic.cgm_session_start_time
         */
        public const val CGM_SESSION_START_TIME: UInt = 0x2AAA_u

        /**
         * CGM Session Run Time
         *
         * 0x2AAB
         *
         * org.bluetooth.characteristic.cgm_session_run_time
         */
        public const val CGM_SESSION_RUN_TIME: UInt = 0x2AAB_u

        /**
         * CGM Specific Ops Control Point
         *
         * 0x2AAC
         *
         * org.bluetooth.characteristic.cgm_specific_ops_control_point
         */
        public const val CGM_SPECIFIC_OPS_CONTROL_POINT: UInt = 0x2AAC_u

        /**
         * Indoor Positioning Configuration
         *
         * 0x2AAD
         *
         * org.bluetooth.characteristic.indoor_positioning_configuration
         */
        public const val INDOOR_POSITIONING_CONFIGURATION: UInt = 0x2AAD_u

        /**
         * Latitude
         *
         * 0x2AAE
         *
         * org.bluetooth.characteristic.latitude
         */
        public const val LATITUDE: UInt = 0x2AAE_u

        /**
         * Longitude
         *
         * 0x2AAF
         *
         * org.bluetooth.characteristic.longitude
         */
        public const val LONGITUDE: UInt = 0x2AAF_u

        /**
         * Local North Coordinate
         *
         * 0x2AB0
         *
         * org.bluetooth.characteristic.local_north_coordinate
         */
        public const val LOCAL_NORTH_COORDINATE: UInt = 0x2AB0_u

        /**
         * Local East Coordinate
         *
         * 0x2AB1
         *
         * org.bluetooth.characteristic.local_east_coordinate
         */
        public const val LOCAL_EAST_COORDINATE: UInt = 0x2AB1_u

        /**
         * Floor Number
         *
         * 0x2AB2
         *
         * org.bluetooth.characteristic.floor_number
         */
        public const val FLOOR_NUMBER: UInt = 0x2AB2_u

        /**
         * Altitude
         *
         * 0x2AB3
         *
         * org.bluetooth.characteristic.altitude
         */
        public const val ALTITUDE: UInt = 0x2AB3_u

        /**
         * Uncertainty
         *
         * 0x2AB4
         *
         * org.bluetooth.characteristic.uncertainty
         */
        public const val UNCERTAINTY: UInt = 0x2AB4_u

        /**
         * Location Name
         *
         * 0x2AB5
         *
         * org.bluetooth.characteristic.location_name
         */
        public const val LOCATION_NAME: UInt = 0x2AB5_u

        /**
         * URI
         *
         * 0x2AB6
         *
         * org.bluetooth.characteristic.uri
         */
        public const val URI: UInt = 0x2AB6_u

        /**
         * HTTP Headers
         *
         * 0x2AB7
         *
         * org.bluetooth.characteristic.http_headers
         */
        public const val HTTP_HEADERS: UInt = 0x2AB7_u

        /**
         * HTTP Status Code
         *
         * 0x2AB8
         *
         * org.bluetooth.characteristic.http_status_code
         */
        public const val HTTP_STATUS_CODE: UInt = 0x2AB8_u

        /**
         * HTTP Entity Body
         *
         * 0x2AB9
         *
         * org.bluetooth.characteristic.http_entity_body
         */
        public const val HTTP_ENTITY_BODY: UInt = 0x2AB9_u

        /**
         * HTTP Control Point
         *
         * 0x2ABA
         *
         * org.bluetooth.characteristic.http_control_point
         */
        public const val HTTP_CONTROL_POINT: UInt = 0x2ABA_u

        /**
         * HTTPS Security
         *
         * 0x2ABB
         *
         * org.bluetooth.characteristic.https_security
         */
        public const val HTTPS_SECURITY: UInt = 0x2ABB_u

        /**
         * TDS Control Point
         *
         * 0x2ABC
         *
         * org.bluetooth.characteristic.tds_control_point
         */
        public const val TDS_CONTROL_POINT: UInt = 0x2ABC_u

        /**
         * OTS Feature
         *
         * 0x2ABD
         *
         * org.bluetooth.characteristic.ots_feature
         */
        public const val OTS_FEATURE: UInt = 0x2ABD_u

        /**
         * Object Name
         *
         * 0x2ABE
         *
         * org.bluetooth.characteristic.object_name
         */
        public const val OBJECT_NAME: UInt = 0x2ABE_u

        /**
         * Object Type
         *
         * 0x2ABF
         *
         * org.bluetooth.characteristic.object_type
         */
        public const val OBJECT_TYPE: UInt = 0x2ABF_u

        /**
         * Object Size
         *
         * 0x2AC0
         *
         * org.bluetooth.characteristic.object_size
         */
        public const val OBJECT_SIZE: UInt = 0x2AC0_u

        /**
         * Object First-Created
         *
         * 0x2AC1
         *
         * org.bluetooth.characteristic.object_first_created
         */
        public const val OBJECT_FIRST_CREATED: UInt = 0x2AC1_u

        /**
         * Object Last-Modified
         *
         * 0x2AC2
         *
         * org.bluetooth.characteristic.object_last_modified
         */
        public const val OBJECT_LAST_MODIFIED: UInt = 0x2AC2_u

        /**
         * Object ID
         *
         * 0x2AC3
         *
         * org.bluetooth.characteristic.object_id
         */
        public const val OBJECT_ID: UInt = 0x2AC3_u

        /**
         * Object Properties
         *
         * 0x2AC4
         *
         * org.bluetooth.characteristic.object_properties
         */
        public const val OBJECT_PROPERTIES: UInt = 0x2AC4_u

        /**
         * Object Action Control Point
         *
         * 0x2AC5
         *
         * org.bluetooth.characteristic.object_action_control_point
         */
        public const val OBJECT_ACTION_CONTROL_POINT: UInt = 0x2AC5_u

        /**
         * Object List Control Point
         *
         * 0x2AC6
         *
         * org.bluetooth.characteristic.object_list_control_point
         */
        public const val OBJECT_LIST_CONTROL_POINT: UInt = 0x2AC6_u

        /**
         * Object List Filter
         *
         * 0x2AC7
         *
         * org.bluetooth.characteristic.object_list_filter
         */
        public const val OBJECT_LIST_FILTER: UInt = 0x2AC7_u

        /**
         * Object Changed
         *
         * 0x2AC8
         *
         * org.bluetooth.characteristic.object_changed
         */
        public const val OBJECT_CHANGED: UInt = 0x2AC8_u

        /**
         * Resolvable Private Address Only
         *
         * 0x2AC9
         *
         * org.bluetooth.characteristic.resolvable_private_address_only
         */
        public const val RESOLVABLE_PRIVATE_ADDRESS_ONLY: UInt = 0x2AC9_u

        /**
         * Fitness Machine Feature
         *
         * 0x2ACC
         *
         * org.bluetooth.characteristic.fitness_machine_feature
         */
        public const val FITNESS_MACHINE_FEATURE: UInt = 0x2ACC_u

        /**
         * Treadmill Data
         *
         * 0x2ACD
         *
         * org.bluetooth.characteristic.treadmill_data
         */
        public const val TREADMILL_DATA: UInt = 0x2ACD_u

        /**
         * Cross Trainer Data
         *
         * 0x2ACE
         *
         * org.bluetooth.characteristic.cross_trainer_data
         */
        public const val CROSS_TRAINER_DATA: UInt = 0x2ACE_u

        /**
         * Step Climber Data
         *
         * 0x2ACF
         *
         * org.bluetooth.characteristic.step_climber_data
         */
        public const val STEP_CLIMBER_DATA: UInt = 0x2ACF_u

        /**
         * Stair Climber Data
         *
         * 0x2AD0
         *
         * org.bluetooth.characteristic.stair_climber_data
         */
        public const val STAIR_CLIMBER_DATA: UInt = 0x2AD0_u

        /**
         * Rower Data
         *
         * 0x2AD1
         *
         * org.bluetooth.characteristic.rower_data
         */
        public const val ROWER_DATA: UInt = 0x2AD1_u

        /**
         * Indoor Bike Data
         *
         * 0x2AD2
         *
         * org.bluetooth.characteristic.indoor_bike_data
         */
        public const val INDOOR_BIKE_DATA: UInt = 0x2AD2_u

        /**
         * Training Status
         *
         * 0x2AD3
         *
         * org.bluetooth.characteristic.training_status
         */
        public const val TRAINING_STATUS: UInt = 0x2AD3_u

        /**
         * Supported Speed Range
         *
         * 0x2AD4
         *
         * org.bluetooth.characteristic.supported_speed_range
         */
        public const val SUPPORTED_SPEED_RANGE: UInt = 0x2AD4_u

        /**
         * Supported Inclination Range
         *
         * 0x2AD5
         *
         * org.bluetooth.characteristic.supported_inclination_range
         */
        public const val SUPPORTED_INCLINATION_RANGE: UInt = 0x2AD5_u

        /**
         * Supported Resistance Level Range
         *
         * 0x2AD6
         *
         * org.bluetooth.characteristic.supported_resistance_level_range
         */
        public const val SUPPORTED_RESISTANCE_LEVEL_RANGE: UInt = 0x2AD6_u

        /**
         * Supported Heart Rate Range
         *
         * 0x2AD7
         *
         * org.bluetooth.characteristic.supported_heart_rate_range
         */
        public const val SUPPORTED_HEART_RATE_RANGE: UInt = 0x2AD7_u

        /**
         * Supported Power Range
         *
         * 0x2AD8
         *
         * org.bluetooth.characteristic.supported_power_range
         */
        public const val SUPPORTED_POWER_RANGE: UInt = 0x2AD8_u

        /**
         * Fitness Machine Control Point
         *
         * 0x2AD9
         *
         * org.bluetooth.characteristic.fitness_machine_control_point
         */
        public const val FITNESS_MACHINE_CONTROL_POINT: UInt = 0x2AD9_u

        /**
         * Fitness Machine Status
         *
         * 0x2ADA
         *
         * org.bluetooth.characteristic.fitness_machine_status
         */
        public const val FITNESS_MACHINE_STATUS: UInt = 0x2ADA_u

        /**
         * Mesh Provisioning Data In
         *
         * 0x2ADB
         *
         * org.bluetooth.characteristic.mesh_provisioning_data_in
         */
        public const val MESH_PROVISIONING_DATA_IN: UInt = 0x2ADB_u

        /**
         * Mesh Provisioning Data Out
         *
         * 0x2ADC
         *
         * org.bluetooth.characteristic.mesh_provisioning_data_out
         */
        public const val MESH_PROVISIONING_DATA_OUT: UInt = 0x2ADC_u

        /**
         * Mesh Proxy Data In
         *
         * 0x2ADD
         *
         * org.bluetooth.characteristic.mesh_proxy_data_in
         */
        public const val MESH_PROXY_DATA_IN: UInt = 0x2ADD_u

        /**
         * Mesh Proxy Data Out
         *
         * 0x2ADE
         *
         * org.bluetooth.characteristic.mesh_proxy_data_out
         */
        public const val MESH_PROXY_DATA_OUT: UInt = 0x2ADE_u

        /**
         * Average Current
         *
         * 0x2AE0
         *
         * org.bluetooth.characteristic.average_current
         */
        public const val AVERAGE_CURRENT: UInt = 0x2AE0_u

        /**
         * Average Voltage
         *
         * 0x2AE1
         *
         * org.bluetooth.characteristic.average_voltage
         */
        public const val AVERAGE_VOLTAGE: UInt = 0x2AE1_u

        /**
         * Boolean
         *
         * 0x2AE2
         *
         * org.bluetooth.characteristic.boolean
         */
        public const val BOOLEAN: UInt = 0x2AE2_u

        /**
         * Chromatic Distance from Planckian
         *
         * 0x2AE3
         *
         * org.bluetooth.characteristic.chromatic_distance_from_planckian
         */
        public const val CHROMATIC_DISTANCE_FROM_PLANCKIAN: UInt = 0x2AE3_u

        /**
         * Chromaticity Coordinates
         *
         * 0x2AE4
         *
         * org.bluetooth.characteristic.chromaticity_coordinates
         */
        public const val CHROMATICITY_COORDINATES: UInt = 0x2AE4_u

        /**
         * Chromaticity in CCT and Duv Values
         *
         * 0x2AE5
         *
         * org.bluetooth.characteristic.chromaticity_in_cct_and_duv_values
         */
        public const val CHROMATICITY_IN_CCT_AND_DUV_VALUES: UInt = 0x2AE5_u

        /**
         * Chromaticity Tolerance
         *
         * 0x2AE6
         *
         * org.bluetooth.characteristic.chromaticity_tolerance
         */
        public const val CHROMATICITY_TOLERANCE: UInt = 0x2AE6_u

        /**
         * CIE 13.3-1995 Color Rendering Index
         *
         * 0x2AE7
         *
         * org.bluetooth.characteristic.cie_13_3_1995_color_rendering_index
         */
        public const val CIE_13_3_1995_COLOR_RENDERING_INDEX: UInt = 0x2AE7_u

        /**
         * Coefficient
         *
         * 0x2AE8
         *
         * org.bluetooth.characteristic.coefficient
         */
        public const val COEFFICIENT: UInt = 0x2AE8_u

        /**
         * Correlated Color Temperature
         *
         * 0x2AE9
         *
         * org.bluetooth.characteristic.correlated_color_temperature
         */
        public const val CORRELATED_COLOR_TEMPERATURE: UInt = 0x2AE9_u

        /**
         * Count 16
         *
         * 0x2AEA
         *
         * org.bluetooth.characteristic.count_16
         */
        public const val COUNT_16: UInt = 0x2AEA_u

        /**
         * Count 24
         *
         * 0x2AEB
         *
         * org.bluetooth.characteristic.count_24
         */
        public const val COUNT_24: UInt = 0x2AEB_u

        /**
         * Country Code
         *
         * 0x2AEC
         *
         * org.bluetooth.characteristic.country_code
         */
        public const val COUNTRY_CODE: UInt = 0x2AEC_u

        /**
         * Date UTC
         *
         * 0x2AED
         *
         * org.bluetooth.characteristic.date_utc
         */
        public const val DATE_UTC: UInt = 0x2AED_u

        /**
         * Electric Current
         *
         * 0x2AEE
         *
         * org.bluetooth.characteristic.electric_current
         */
        public const val ELECTRIC_CURRENT: UInt = 0x2AEE_u

        /**
         * Electric Current Range
         *
         * 0x2AEF
         *
         * org.bluetooth.characteristic.electric_current_range
         */
        public const val ELECTRIC_CURRENT_RANGE: UInt = 0x2AEF_u

        /**
         * Electric Current Specification
         *
         * 0x2AF0
         *
         * org.bluetooth.characteristic.electric_current_specification
         */
        public const val ELECTRIC_CURRENT_SPECIFICATION: UInt = 0x2AF0_u

        /**
         * Electric Current Statistics
         *
         * 0x2AF1
         *
         * org.bluetooth.characteristic.electric_current_statistics
         */
        public const val ELECTRIC_CURRENT_STATISTICS: UInt = 0x2AF1_u

        /**
         * Energy
         *
         * 0x2AF2
         *
         * org.bluetooth.characteristic.energy
         */
        public const val ENERGY: UInt = 0x2AF2_u

        /**
         * Energy in a Period of Day
         *
         * 0x2AF3
         *
         * org.bluetooth.characteristic.energy_in_a_period_of_day
         */
        public const val ENERGY_IN_A_PERIOD_OF_DAY: UInt = 0x2AF3_u

        /**
         * Event Statistics
         *
         * 0x2AF4
         *
         * org.bluetooth.characteristic.event_statistics
         */
        public const val EVENT_STATISTICS: UInt = 0x2AF4_u

        /**
         * Fixed String 16
         *
         * 0x2AF5
         *
         * org.bluetooth.characteristic.fixed_string_16
         */
        public const val FIXED_STRING_16: UInt = 0x2AF5_u

        /**
         * Fixed String 24
         *
         * 0x2AF6
         *
         * org.bluetooth.characteristic.fixed_string_24
         */
        public const val FIXED_STRING_24: UInt = 0x2AF6_u

        /**
         * Fixed String 36
         *
         * 0x2AF7
         *
         * org.bluetooth.characteristic.fixed_string_36
         */
        public const val FIXED_STRING_36: UInt = 0x2AF7_u

        /**
         * Fixed String 8
         *
         * 0x2AF8
         *
         * org.bluetooth.characteristic.fixed_string_8
         */
        public const val FIXED_STRING_8: UInt = 0x2AF8_u

        /**
         * Generic Level
         *
         * 0x2AF9
         *
         * org.bluetooth.characteristic.generic_level
         */
        public const val GENERIC_LEVEL: UInt = 0x2AF9_u

        /**
         * Global Trade Item Number
         *
         * 0x2AFA
         *
         * org.bluetooth.characteristic.global_trade_item_number
         */
        public const val GLOBAL_TRADE_ITEM_NUMBER: UInt = 0x2AFA_u

        /**
         * Illuminance
         *
         * 0x2AFB
         *
         * org.bluetooth.characteristic.illuminance
         */
        public const val ILLUMINANCE: UInt = 0x2AFB_u

        /**
         * Luminous Efficacy
         *
         * 0x2AFC
         *
         * org.bluetooth.characteristic.luminous_efficacy
         */
        public const val LUMINOUS_EFFICACY: UInt = 0x2AFC_u

        /**
         * Luminous Energy
         *
         * 0x2AFD
         *
         * org.bluetooth.characteristic.luminous_energy
         */
        public const val LUMINOUS_ENERGY: UInt = 0x2AFD_u

        /**
         * Luminous Exposure
         *
         * 0x2AFE
         *
         * org.bluetooth.characteristic.luminous_exposure
         */
        public const val LUMINOUS_EXPOSURE: UInt = 0x2AFE_u

        /**
         * Luminous Flux
         *
         * 0x2AFF
         *
         * org.bluetooth.characteristic.luminous_flux
         */
        public const val LUMINOUS_FLUX: UInt = 0x2AFF_u

        /**
         * Luminous Flux Range
         *
         * 0x2B00
         *
         * org.bluetooth.characteristic.luminous_flux_range
         */
        public const val LUMINOUS_FLUX_RANGE: UInt = 0x2B00_u

        /**
         * Luminous Intensity
         *
         * 0x2B01
         *
         * org.bluetooth.characteristic.luminous_intensity
         */
        public const val LUMINOUS_INTENSITY: UInt = 0x2B01_u

        /**
         * Mass Flow
         *
         * 0x2B02
         *
         * org.bluetooth.characteristic.mass_flow
         */
        public const val MASS_FLOW: UInt = 0x2B02_u

        /**
         * Perceived Lightness
         *
         * 0x2B03
         *
         * org.bluetooth.characteristic.perceived_lightness
         */
        public const val PERCEIVED_LIGHTNESS: UInt = 0x2B03_u

        /**
         * Percentage 8
         *
         * 0x2B04
         *
         * org.bluetooth.characteristic.percentage_8
         */
        public const val PERCENTAGE_8: UInt = 0x2B04_u

        /**
         * Power
         *
         * 0x2B05
         *
         * org.bluetooth.characteristic.power
         */
        public const val POWER: UInt = 0x2B05_u

        /**
         * Power Specification
         *
         * 0x2B06
         *
         * org.bluetooth.characteristic.power_specification
         */
        public const val POWER_SPECIFICATION: UInt = 0x2B06_u

        /**
         * Relative Runtime in a Current Range
         *
         * 0x2B07
         *
         * org.bluetooth.characteristic.relative_runtime_in_a_current_range
         */
        public const val RELATIVE_RUNTIME_IN_A_CURRENT_RANGE: UInt = 0x2B07_u

        /**
         * Relative Runtime in a Generic Level Range
         *
         * 0x2B08
         *
         * org.bluetooth.characteristic.relative_runtime_in_a_generic_level_range
         */
        public const val RELATIVE_RUNTIME_IN_A_GENERIC_LEVEL_RANGE: UInt = 0x2B08_u

        /**
         * Relative Value in a Voltage Range
         *
         * 0x2B09
         *
         * org.bluetooth.characteristic.relative_value_in_a_voltage_range
         */
        public const val RELATIVE_VALUE_IN_A_VOLTAGE_RANGE: UInt = 0x2B09_u

        /**
         * Relative Value in an Illuminance Range
         *
         * 0x2B0A
         *
         * org.bluetooth.characteristic.relative_value_in_an_illuminance_range
         */
        public const val RELATIVE_VALUE_IN_AN_ILLUMINANCE_RANGE: UInt = 0x2B0A_u

        /**
         * Relative Value in a Period of Day
         *
         * 0x2B0B
         *
         * org.bluetooth.characteristic.relative_value_in_a_period_of_day
         */
        public const val RELATIVE_VALUE_IN_A_PERIOD_OF_DAY: UInt = 0x2B0B_u

        /**
         * Relative Value in a Temperature Range
         *
         * 0x2B0C
         *
         * org.bluetooth.characteristic.relative_value_in_a_temperature_range
         */
        public const val RELATIVE_VALUE_IN_A_TEMPERATURE_RANGE: UInt = 0x2B0C_u

        /**
         * Temperature 8
         *
         * 0x2B0D
         *
         * org.bluetooth.characteristic.temperature_8
         */
        public const val TEMPERATURE_8: UInt = 0x2B0D_u

        /**
         * Temperature 8 in a Period of Day
         *
         * 0x2B0E
         *
         * org.bluetooth.characteristic.temperature_8_in_a_period_of_day
         */
        public const val TEMPERATURE_8_IN_A_PERIOD_OF_DAY: UInt = 0x2B0E_u

        /**
         * Temperature 8 Statistics
         *
         * 0x2B0F
         *
         * org.bluetooth.characteristic.temperature_8_statistics
         */
        public const val TEMPERATURE_8_STATISTICS: UInt = 0x2B0F_u

        /**
         * Temperature Range
         *
         * 0x2B10
         *
         * org.bluetooth.characteristic.temperature_range
         */
        public const val TEMPERATURE_RANGE: UInt = 0x2B10_u

        /**
         * Temperature Statistics
         *
         * 0x2B11
         *
         * org.bluetooth.characteristic.temperature_statistics
         */
        public const val TEMPERATURE_STATISTICS: UInt = 0x2B11_u

        /**
         * Time Decihour 8
         *
         * 0x2B12
         *
         * org.bluetooth.characteristic.time_decihour_8
         */
        public const val TIME_DECIHOUR_8: UInt = 0x2B12_u

        /**
         * Time Exponential 8
         *
         * 0x2B13
         *
         * org.bluetooth.characteristic.time_exponential_8
         */
        public const val TIME_EXPONENTIAL_8: UInt = 0x2B13_u

        /**
         * Time Hour 24
         *
         * 0x2B14
         *
         * org.bluetooth.characteristic.time_hour_24
         */
        public const val TIME_HOUR_24: UInt = 0x2B14_u

        /**
         * Time Millisecond 24
         *
         * 0x2B15
         *
         * org.bluetooth.characteristic.time_millisecond_24
         */
        public const val TIME_MILLISECOND_24: UInt = 0x2B15_u

        /**
         * Time Second 16
         *
         * 0x2B16
         *
         * org.bluetooth.characteristic.time_second_16
         */
        public const val TIME_SECOND_16: UInt = 0x2B16_u

        /**
         * Time Second 8
         *
         * 0x2B17
         *
         * org.bluetooth.characteristic.time_second_8
         */
        public const val TIME_SECOND_8: UInt = 0x2B17_u

        /**
         * Voltage
         *
         * 0x2B18
         *
         * org.bluetooth.characteristic.voltage
         */
        public const val VOLTAGE: UInt = 0x2B18_u

        /**
         * Voltage Specification
         *
         * 0x2B19
         *
         * org.bluetooth.characteristic.voltage_specification
         */
        public const val VOLTAGE_SPECIFICATION: UInt = 0x2B19_u

        /**
         * Voltage Statistics
         *
         * 0x2B1A
         *
         * org.bluetooth.characteristic.voltage_statistics
         */
        public const val VOLTAGE_STATISTICS: UInt = 0x2B1A_u

        /**
         * Volume Flow
         *
         * 0x2B1B
         *
         * org.bluetooth.characteristic.volume_flow
         */
        public const val VOLUME_FLOW: UInt = 0x2B1B_u

        /**
         * Chromaticity Coordinate
         *
         * 0x2B1C
         *
         * org.bluetooth.characteristic.chromaticity_coordinate
         */
        public const val CHROMATICITY_COORDINATE: UInt = 0x2B1C_u

        /**
         * RC Feature
         *
         * 0x2B1D
         *
         * org.bluetooth.characteristic.rc_feature
         */
        public const val RC_FEATURE: UInt = 0x2B1D_u

        /**
         * RC Settings
         *
         * 0x2B1E
         *
         * org.bluetooth.characteristic.rc_settings
         */
        public const val RC_SETTINGS: UInt = 0x2B1E_u

        /**
         * Reconnection Configuration Control Point
         *
         * 0x2B1F
         *
         * org.bluetooth.characteristic.reconnection_configuration_control_point
         */
        public const val RECONNECTION_CONFIGURATION_CONTROL_POINT: UInt = 0x2B1F_u

        /**
         * IDD Status Changed
         *
         * 0x2B20
         *
         * org.bluetooth.characteristic.idd_status_changed
         */
        public const val IDD_STATUS_CHANGED: UInt = 0x2B20_u

        /**
         * IDD Status
         *
         * 0x2B21
         *
         * org.bluetooth.characteristic.idd_status
         */
        public const val IDD_STATUS: UInt = 0x2B21_u

        /**
         * IDD Annunciation Status
         *
         * 0x2B22
         *
         * org.bluetooth.characteristic.idd_annunciation_status
         */
        public const val IDD_ANNUNCIATION_STATUS: UInt = 0x2B22_u

        /**
         * IDD Features
         *
         * 0x2B23
         *
         * org.bluetooth.characteristic.idd_features
         */
        public const val IDD_FEATURES: UInt = 0x2B23_u

        /**
         * IDD Status Reader Control Point
         *
         * 0x2B24
         *
         * org.bluetooth.characteristic.idd_status_reader_control_point
         */
        public const val IDD_STATUS_READER_CONTROL_POINT: UInt = 0x2B24_u

        /**
         * IDD Command Control Point
         *
         * 0x2B25
         *
         * org.bluetooth.characteristic.idd_command_control_point
         */
        public const val IDD_COMMAND_CONTROL_POINT: UInt = 0x2B25_u

        /**
         * IDD Command Data
         *
         * 0x2B26
         *
         * org.bluetooth.characteristic.idd_command_data
         */
        public const val IDD_COMMAND_DATA: UInt = 0x2B26_u

        /**
         * IDD Record Access Control Point
         *
         * 0x2B27
         *
         * org.bluetooth.characteristic.idd_record_access_control_point
         */
        public const val IDD_RECORD_ACCESS_CONTROL_POINT: UInt = 0x2B27_u

        /**
         * IDD History Data
         *
         * 0x2B28
         *
         * org.bluetooth.characteristic.idd_history_data
         */
        public const val IDD_HISTORY_DATA: UInt = 0x2B28_u

        /**
         * Client Supported Features
         *
         * 0x2B29
         *
         * org.bluetooth.characteristic.client_supported_features
         */
        public const val CLIENT_SUPPORTED_FEATURES: UInt = 0x2B29_u

        /**
         * Database Hash
         *
         * 0x2B2A
         *
         * org.bluetooth.characteristic.database_hash
         */
        public const val DATABASE_HASH: UInt = 0x2B2A_u

        /**
         * BSS Control Point
         *
         * 0x2B2B
         *
         * org.bluetooth.characteristic.bss_control_point
         */
        public const val BSS_CONTROL_POINT: UInt = 0x2B2B_u

        /**
         * BSS Response
         *
         * 0x2B2C
         *
         * org.bluetooth.characteristic.bss_response
         */
        public const val BSS_RESPONSE: UInt = 0x2B2C_u

        /**
         * Emergency ID
         *
         * 0x2B2D
         *
         * org.bluetooth.characteristic.emergency_id
         */
        public const val EMERGENCY_ID: UInt = 0x2B2D_u

        /**
         * Emergency Text
         *
         * 0x2B2E
         *
         * org.bluetooth.characteristic.emergency_text
         */
        public const val EMERGENCY_TEXT: UInt = 0x2B2E_u

        /**
         * ACS Status
         *
         * 0x2B2F
         *
         * org.bluetooth.characteristic.acs_status
         */
        public const val ACS_STATUS: UInt = 0x2B2F_u

        /**
         * ACS Data In
         *
         * 0x2B30
         *
         * org.bluetooth.characteristic.acs_data_in
         */
        public const val ACS_DATA_IN: UInt = 0x2B30_u

        /**
         * ACS Data Out Notify
         *
         * 0x2B31
         *
         * org.bluetooth.characteristic.acs_data_out_notify
         */
        public const val ACS_DATA_OUT_NOTIFY: UInt = 0x2B31_u

        /**
         * ACS Data Out Indicate
         *
         * 0x2B32
         *
         * org.bluetooth.characteristic.acs_data_out_indicate
         */
        public const val ACS_DATA_OUT_INDICATE: UInt = 0x2B32_u

        /**
         * ACS Control Point
         *
         * 0x2B33
         *
         * org.bluetooth.characteristic.acs_control_point
         */
        public const val ACS_CONTROL_POINT: UInt = 0x2B33_u

        /**
         * Enhanced Blood Pressure Measurement
         *
         * 0x2B34
         *
         * org.bluetooth.characteristic.enhanced_blood_pressure_measurement
         */
        public const val ENHANCED_BLOOD_PRESSURE_MEASUREMENT: UInt = 0x2B34_u

        /**
         * Enhanced Intermediate Cuff Pressure
         *
         * 0x2B35
         *
         * org.bluetooth.characteristic.enhanced_intermediate_cuff_pressure
         */
        public const val ENHANCED_INTERMEDIATE_CUFF_PRESSURE: UInt = 0x2B35_u

        /**
         * Blood Pressure Record
         *
         * 0x2B36
         *
         * org.bluetooth.characteristic.blood_pressure_record
         */
        public const val BLOOD_PRESSURE_RECORD: UInt = 0x2B36_u

        /**
         * Registered User
         *
         * 0x2B37
         *
         * org.bluetooth.characteristic.registered_user
         */
        public const val REGISTERED_USER: UInt = 0x2B37_u

        /**
         * BR-EDR Handover Data
         *
         * 0x2B38
         *
         * org.bluetooth.characteristic.br_edr_handover_data
         */
        public const val BR_EDR_HANDOVER_DATA: UInt = 0x2B38_u

        /**
         * Bluetooth SIG Data
         *
         * 0x2B39
         *
         * org.bluetooth.characteristic.bluetooth_sig_data
         */
        public const val BLUETOOTH_SIG_DATA: UInt = 0x2B39_u

        /**
         * Server Supported Features
         *
         * 0x2B3A
         *
         * org.bluetooth.characteristic.server_supported_features
         */
        public const val SERVER_SUPPORTED_FEATURES: UInt = 0x2B3A_u

        /**
         * Physical Activity Monitor Features
         *
         * 0x2B3B
         *
         * org.bluetooth.characteristic.physical_activity_monitor_features
         */
        public const val PHYSICAL_ACTIVITY_MONITOR_FEATURES: UInt = 0x2B3B_u

        /**
         * General Activity Instantaneous Data
         *
         * 0x2B3C
         *
         * org.bluetooth.characteristic.general_activity_instantaneous_data
         */
        public const val GENERAL_ACTIVITY_INSTANTANEOUS_DATA: UInt = 0x2B3C_u

        /**
         * General Activity Summary Data
         *
         * 0x2B3D
         *
         * org.bluetooth.characteristic.general_activity_summary_data
         */
        public const val GENERAL_ACTIVITY_SUMMARY_DATA: UInt = 0x2B3D_u

        /**
         * CardioRespiratory Activity Instantaneous Data
         *
         * 0x2B3E
         *
         * org.bluetooth.characteristic.cardiorespiratory_activity_instantaneous_data
         */
        public const val CARDIO_RESPIRATORY_ACTIVITY_INSTANTANEOUS_DATA: UInt = 0x2B3E_u

        /**
         * CardioRespiratory Activity Summary Data
         *
         * 0x2B3F
         *
         * org.bluetooth.characteristic.cardiorespiratory_activity_summary_data
         */
        public const val CARDIO_RESPIRATORY_ACTIVITY_SUMMARY_DATA: UInt = 0x2B3F_u

        /**
         * Step Counter Activity Summary Data
         *
         * 0x2B40
         *
         * org.bluetooth.characteristic.step_counter_activity_summary_data
         */
        public const val STEP_COUNTER_ACTIVITY_SUMMARY_DATA: UInt = 0x2B40_u

        /**
         * Sleep Activity Instantaneous Data
         *
         * 0x2B41
         *
         * org.bluetooth.characteristic.sleep_activity_instantaneous_data
         */
        public const val SLEEP_ACTIVITY_INSTANTANEOUS_DATA: UInt = 0x2B41_u

        /**
         * Sleep Activity Summary Data
         *
         * 0x2B42
         *
         * org.bluetooth.characteristic.sleep_activity_summary_data
         */
        public const val SLEEP_ACTIVITY_SUMMARY_DATA: UInt = 0x2B42_u

        /**
         * Physical Activity Monitor Control Point
         *
         * 0x2B43
         *
         * org.bluetooth.characteristic.physical_activity_monitor_control_point
         */
        public const val PHYSICAL_ACTIVITY_MONITOR_CONTROL_POINT: UInt = 0x2B43_u

        /**
         * Physical Activity Current Session
         *
         * 0x2B44
         *
         * org.bluetooth.characteristic.physical_activity_current_session
         */
        public const val PHYSICAL_ACTIVITY_CURRENT_SESSION: UInt = 0x2B44_u

        /**
         * Physical Activity Session Descriptor
         *
         * 0x2B45
         *
         * org.bluetooth.characteristic.physical_activity_session_descriptor
         */
        public const val PHYSICAL_ACTIVITY_SESSION_DESCRIPTOR: UInt = 0x2B45_u

        /**
         * Preferred Units
         *
         * 0x2B46
         *
         * org.bluetooth.characteristic.preferred_units
         */
        public const val PREFERRED_UNITS: UInt = 0x2B46_u

        /**
         * High Resolution Height
         *
         * 0x2B47
         *
         * org.bluetooth.characteristic.high_resolution_height
         */
        public const val HIGH_RESOLUTION_HEIGHT: UInt = 0x2B47_u

        /**
         * Middle Name
         *
         * 0x2B48
         *
         * org.bluetooth.characteristic.middle_name
         */
        public const val MIDDLE_NAME: UInt = 0x2B48_u

        /**
         * Stride Length
         *
         * 0x2B49
         *
         * org.bluetooth.characteristic.stride_length
         */
        public const val STRIDE_LENGTH: UInt = 0x2B49_u

        /**
         * Handedness
         *
         * 0x2B4A
         *
         * org.bluetooth.characteristic.handedness
         */
        public const val HANDEDNESS: UInt = 0x2B4A_u

        /**
         * Device Wearing Position
         *
         * 0x2B4B
         *
         * org.bluetooth.characteristic.device_wearing_position
         */
        public const val DEVICE_WEARING_POSITION: UInt = 0x2B4B_u

        /**
         * Four Zone Heart Rate Limits
         *
         * 0x2B4C
         *
         * org.bluetooth.characteristic.four_zone_heart_rate_limits
         */
        public const val FOUR_ZONE_HEART_RATE_LIMITS: UInt = 0x2B4C_u

        /**
         * High Intensity Exercise Threshold
         *
         * 0x2B4D
         *
         * org.bluetooth.characteristic.high_intensity_exercise_threshold
         */
        public const val HIGH_INTENSITY_EXERCISE_THRESHOLD: UInt = 0x2B4D_u

        /**
         * Activity Goal
         *
         * 0x2B4E
         *
         * org.bluetooth.characteristic.activity_goal
         */
        public const val ACTIVITY_GOAL: UInt = 0x2B4E_u

        /**
         * Sedentary Interval Notification
         *
         * 0x2B4F
         *
         * org.bluetooth.characteristic.sedentary_interval_notification
         */
        public const val SEDENTARY_INTERVAL_NOTIFICATION: UInt = 0x2B4F_u

        /**
         * Caloric Intake
         *
         * 0x2B50
         *
         * org.bluetooth.characteristic.caloric_intake
         */
        public const val CALORIC_INTAKE: UInt = 0x2B50_u

        /**
         * TMAP Role
         *
         * 0x2B51
         *
         * org.bluetooth.characteristic.tmap_role
         */
        public const val TMAP_ROLE: UInt = 0x2B51_u

        /**
         * Audio Input State
         *
         * 0x2B77
         *
         * org.bluetooth.characteristic.audio_input_state
         */
        public const val AUDIO_INPUT_STATE: UInt = 0x2B77_u

        /**
         * Gain Settings Attribute
         *
         * 0x2B78
         *
         * org.bluetooth.characteristic.gain_settings_attribute
         */
        public const val GAIN_SETTINGS_ATTRIBUTE: UInt = 0x2B78_u

        /**
         * Audio Input Type
         *
         * 0x2B79
         *
         * org.bluetooth.characteristic.audio_input_type
         */
        public const val AUDIO_INPUT_TYPE: UInt = 0x2B79_u

        /**
         * Audio Input Status
         *
         * 0x2B7A
         *
         * org.bluetooth.characteristic.audio_input_status
         */
        public const val AUDIO_INPUT_STATUS: UInt = 0x2B7A_u

        /**
         * Audio Input Control Point
         *
         * 0x2B7B
         *
         * org.bluetooth.characteristic.audio_input_control_point
         */
        public const val AUDIO_INPUT_CONTROL_POINT: UInt = 0x2B7B_u

        /**
         * Audio Input Description
         *
         * 0x2B7C
         *
         * org.bluetooth.characteristic.audio_input_description
         */
        public const val AUDIO_INPUT_DESCRIPTION: UInt = 0x2B7C_u

        /**
         * Volume State
         *
         * 0x2B7D
         *
         * org.bluetooth.characteristic.volume_state
         */
        public const val VOLUME_STATE: UInt = 0x2B7D_u

        /**
         * Volume Control Point
         *
         * 0x2B7E
         *
         * org.bluetooth.characteristic.volume_control_point
         */
        public const val VOLUME_CONTROL_POINT: UInt = 0x2B7E_u

        /**
         * Volume Flags
         *
         * 0x2B7F
         *
         * org.bluetooth.characteristic.volume_flags
         */
        public const val VOLUME_FLAGS: UInt = 0x2B7F_u

        /**
         * Volume Offset State
         *
         * 0x2B80
         *
         * org.bluetooth.characteristic.volume_offset_state
         */
        public const val VOLUME_OFFSET_STATE: UInt = 0x2B80_u

        /**
         * Audio Location
         *
         * 0x2B81
         *
         * org.bluetooth.characteristic.audio_location
         */
        public const val AUDIO_LOCATION: UInt = 0x2B81_u

        /**
         * Volume Offset Control Point
         *
         * 0x2B82
         *
         * org.bluetooth.characteristic.volume_offset_control_point
         */
        public const val VOLUME_OFFSET_CONTROL_POINT: UInt = 0x2B82_u

        /**
         * Audio Output Description
         *
         * 0x2B83
         *
         * org.bluetooth.characteristic.audio_output_description
         */
        public const val AUDIO_OUTPUT_DESCRIPTION: UInt = 0x2B83_u

        /**
         * Set Identity Resolving Key
         *
         * 0x2B84
         *
         * org.bluetooth.characteristic.set_identity_resolving_key
         */
        public const val SET_IDENTITY_RESOLVING_KEY: UInt = 0x2B84_u

        /**
         * Coordinated Set Size
         *
         * 0x2B85
         *
         * org.bluetooth.characteristic.size_characteristic
         */
        public const val COORDINATED_SET_SIZE: UInt = 0x2B85_u

        /**
         * Set Member Lock
         *
         * 0x2B86
         *
         * org.bluetooth.characteristic.lock_characteristic
         */
        public const val SET_MEMBER_LOCK: UInt = 0x2B86_u

        /**
         * Set Member Rank
         *
         * 0x2B87
         *
         * org.bluetooth.characteristic.rank_characteristic
         */
        public const val SET_MEMBER_RANK: UInt = 0x2B87_u

        /**
         * Encrypted Data Key Material
         *
         * 0x2B88
         *
         * org.bluetooth.characteristic.encrypted_data_key_material
         */
        public const val ENCRYPTED_DATA_KEY_MATERIAL: UInt = 0x2B88_u

        /**
         * Apparent Energy 32
         *
         * 0x2B89
         *
         * org.bluetooth.characteristic.apparent_energy_32
         */
        public const val APPARENT_ENERGY_32: UInt = 0x2B89_u

        /**
         * Apparent Power
         *
         * 0x2B8A
         *
         * org.bluetooth.characteristic.apparent_power
         */
        public const val APPARENT_POWER: UInt = 0x2B8A_u

        /**
         * Live Health Observations
         *
         * 0x2B8B
         *
         * org.bluetooth.characteristic.live_health_observations
         */
        public const val LIVE_HEALTH_OBSERVATIONS: UInt = 0x2B8B_u

        /**
         * CO\textsubscript{2} Concentration
         *
         * 0x2B8C
         *
         * org.bluetooth.characteristic.co2_concentration
         */
        public const val CO_TEXTSUBSCRIPT_2_CONCENTRATION: UInt = 0x2B8C_u

        /**
         * Cosine of the Angle
         *
         * 0x2B8D
         *
         * org.bluetooth.characteristic.cosine_of_the_angle
         */
        public const val COSINE_OF_THE_ANGLE: UInt = 0x2B8D_u

        /**
         * Device Time Feature
         *
         * 0x2B8E
         *
         * org.bluetooth.characteristic.device_time_feature
         */
        public const val DEVICE_TIME_FEATURE: UInt = 0x2B8E_u

        /**
         * Device Time Parameters
         *
         * 0x2B8F
         *
         * org.bluetooth.characteristic.device_time_parameters
         */
        public const val DEVICE_TIME_PARAMETERS: UInt = 0x2B8F_u

        /**
         * Device Time
         *
         * 0x2B90
         *
         * org.bluetooth.characteristic.device_time
         */
        public const val DEVICE_TIME: UInt = 0x2B90_u

        /**
         * Device Time Control Point
         *
         * 0x2B91
         *
         * org.bluetooth.characteristic.device_time_control_point
         */
        public const val DEVICE_TIME_CONTROL_POINT: UInt = 0x2B91_u

        /**
         * Time Change Log Data
         *
         * 0x2B92
         *
         * org.bluetooth.characteristic.time_change_log_data
         */
        public const val TIME_CHANGE_LOG_DATA: UInt = 0x2B92_u

        /**
         * Media Player Name
         *
         * 0x2B93
         *
         * org.bluetooth.characteristic.media_player_name
         */
        public const val MEDIA_PLAYER_NAME: UInt = 0x2B93_u

        /**
         * Media Player Icon Object ID
         *
         * 0x2B94
         *
         * org.bluetooth.characteristic.media_player_icon_object_id
         */
        public const val MEDIA_PLAYER_ICON_OBJECT_ID: UInt = 0x2B94_u

        /**
         * Media Player Icon URL
         *
         * 0x2B95
         *
         * org.bluetooth.characteristic.media_player_icon_url
         */
        public const val MEDIA_PLAYER_ICON_URL: UInt = 0x2B95_u

        /**
         * Track Changed
         *
         * 0x2B96
         *
         * org.bluetooth.characteristic.track_changed
         */
        public const val TRACK_CHANGED: UInt = 0x2B96_u

        /**
         * Track Title
         *
         * 0x2B97
         *
         * org.bluetooth.characteristic.track_title
         */
        public const val TRACK_TITLE: UInt = 0x2B97_u

        /**
         * Track Duration
         *
         * 0x2B98
         *
         * org.bluetooth.characteristic.track_duration
         */
        public const val TRACK_DURATION: UInt = 0x2B98_u

        /**
         * Track Position
         *
         * 0x2B99
         *
         * org.bluetooth.characteristic.track_position
         */
        public const val TRACK_POSITION: UInt = 0x2B99_u

        /**
         * Playback Speed
         *
         * 0x2B9A
         *
         * org.bluetooth.characteristic.playback_speed
         */
        public const val PLAYBACK_SPEED: UInt = 0x2B9A_u

        /**
         * Seeking Speed
         *
         * 0x2B9B
         *
         * org.bluetooth.characteristic.seeking_speed
         */
        public const val SEEKING_SPEED: UInt = 0x2B9B_u

        /**
         * Current Track Segments Object ID
         *
         * 0x2B9C
         *
         * org.bluetooth.characteristic.current_track_segments_object_id
         */
        public const val CURRENT_TRACK_SEGMENTS_OBJECT_ID: UInt = 0x2B9C_u

        /**
         * Current Track Object ID
         *
         * 0x2B9D
         *
         * org.bluetooth.characteristic.current_track_object_id
         */
        public const val CURRENT_TRACK_OBJECT_ID: UInt = 0x2B9D_u

        /**
         * Next Track Object ID
         *
         * 0x2B9E
         *
         * org.bluetooth.characteristic.next_track_object_id
         */
        public const val NEXT_TRACK_OBJECT_ID: UInt = 0x2B9E_u

        /**
         * Parent Group Object ID
         *
         * 0x2B9F
         *
         * org.bluetooth.characteristic.parent_group_object_id
         */
        public const val PARENT_GROUP_OBJECT_ID: UInt = 0x2B9F_u

        /**
         * Current Group Object ID
         *
         * 0x2BA0
         *
         * org.bluetooth.characteristic.current_group_object_id
         */
        public const val CURRENT_GROUP_OBJECT_ID: UInt = 0x2BA0_u

        /**
         * Playing Order
         *
         * 0x2BA1
         *
         * org.bluetooth.characteristic.playing_order
         */
        public const val PLAYING_ORDER: UInt = 0x2BA1_u

        /**
         * Playing Orders Supported
         *
         * 0x2BA2
         *
         * org.bluetooth.characteristic.playing_orders_supported
         */
        public const val PLAYING_ORDERS_SUPPORTED: UInt = 0x2BA2_u

        /**
         * Media State
         *
         * 0x2BA3
         *
         * org.bluetooth.characteristic.media_state
         */
        public const val MEDIA_STATE: UInt = 0x2BA3_u

        /**
         * Media Control Point
         *
         * 0x2BA4
         *
         * org.bluetooth.characteristic.media_control_point
         */
        public const val MEDIA_CONTROL_POINT: UInt = 0x2BA4_u

        /**
         * Media Control Point Opcodes Supported
         *
         * 0x2BA5
         *
         * org.bluetooth.characteristic.media_control_point_opcodes_supported
         */
        public const val MEDIA_CONTROL_POINT_OPCODES_SUPPORTED: UInt = 0x2BA5_u

        /**
         * Search Results Object ID
         *
         * 0x2BA6
         *
         * org.bluetooth.characteristic.search_results_object_id
         */
        public const val SEARCH_RESULTS_OBJECT_ID: UInt = 0x2BA6_u

        /**
         * Search Control Point
         *
         * 0x2BA7
         *
         * org.bluetooth.characteristic.search_control_point
         */
        public const val SEARCH_CONTROL_POINT: UInt = 0x2BA7_u

        /**
         * Energy 32
         *
         * 0x2BA8
         *
         * org.bluetooth.characteristic.energy_32
         */
        public const val ENERGY_32: UInt = 0x2BA8_u

        /**
         * Constant Tone Extension Enable
         *
         * 0x2BAD
         *
         * org.bluetooth.characteristic.constant_tone_extension_enable
         */
        public const val CONSTANT_TONE_EXTENSION_ENABLE: UInt = 0x2BAD_u

        /**
         * Advertising Constant Tone Extension Minimum Length
         *
         * 0x2BAE
         *
         * org.bluetooth.characteristic.advertising_constant_tone_extension_minimum_length
         */
        public const val ADVERTISING_CONSTANT_TONE_EXTENSION_MINIMUM_LENGTH: UInt = 0x2BAE_u

        /**
         * Advertising Constant Tone Extension Minimum Transmit Count
         *
         * 0x2BAF
         *
         * org.bluetooth.characteristic.advertising_constant_tone_extension_minimum_transmit_count
         */
        public const val ADVERTISING_CONSTANT_TONE_EXTENSION_MINIMUM_TRANSMIT_COUNT: UInt = 0x2BAF_u

        /**
         * Advertising Constant Tone Extension Transmit Duration
         *
         * 0x2BB0
         *
         * org.bluetooth.characteristic.advertising_constant_tone_extension_transmit_duration
         */
        public const val ADVERTISING_CONSTANT_TONE_EXTENSION_TRANSMIT_DURATION: UInt = 0x2BB0_u

        /**
         * Advertising Constant Tone Extension Interval
         *
         * 0x2BB1
         *
         * org.bluetooth.characteristic.advertising_constant_tone_extension_interval
         */
        public const val ADVERTISING_CONSTANT_TONE_EXTENSION_INTERVAL: UInt = 0x2BB1_u

        /**
         * Advertising Constant Tone Extension PHY
         *
         * 0x2BB2
         *
         * org.bluetooth.characteristic.advertising_constant_tone_extension_phy
         */
        public const val ADVERTISING_CONSTANT_TONE_EXTENSION_PHY: UInt = 0x2BB2_u

        /**
         * Bearer Provider Name
         *
         * 0x2BB3
         *
         * org.bluetooth.characteristic.bearer_provider_name
         */
        public const val BEARER_PROVIDER_NAME: UInt = 0x2BB3_u

        /**
         * Bearer UCI
         *
         * 0x2BB4
         *
         * org.bluetooth.characteristic.bearer_uci
         */
        public const val BEARER_UCI: UInt = 0x2BB4_u

        /**
         * Bearer Technology
         *
         * 0x2BB5
         *
         * org.bluetooth.characteristic.bearer_technology
         */
        public const val BEARER_TECHNOLOGY: UInt = 0x2BB5_u

        /**
         * Bearer URI Schemes Supported List
         *
         * 0x2BB6
         *
         * org.bluetooth.characteristic.bearer_uri_schemes_supported_list
         */
        public const val BEARER_URI_SCHEMES_SUPPORTED_LIST: UInt = 0x2BB6_u

        /**
         * Bearer Signal Strength
         *
         * 0x2BB7
         *
         * org.bluetooth.characteristic.bearer_signal_strength
         */
        public const val BEARER_SIGNAL_STRENGTH: UInt = 0x2BB7_u

        /**
         * Bearer Signal Strength Reporting Interval
         *
         * 0x2BB8
         *
         * org.bluetooth.characteristic.bearer_signal_strength_reporting_interval
         */
        public const val BEARER_SIGNAL_STRENGTH_REPORTING_INTERVAL: UInt = 0x2BB8_u

        /**
         * Bearer List Current Calls
         *
         * 0x2BB9
         *
         * org.bluetooth.characteristic.bearer_list_current_calls
         */
        public const val BEARER_LIST_CURRENT_CALLS: UInt = 0x2BB9_u

        /**
         * Content Control ID
         *
         * 0x2BBA
         *
         * org.bluetooth.characteristic.content_control_id
         */
        public const val CONTENT_CONTROL_ID: UInt = 0x2BBA_u

        /**
         * Status Flags
         *
         * 0x2BBB
         *
         * org.bluetooth.characteristic.status_flags
         */
        public const val STATUS_FLAGS: UInt = 0x2BBB_u

        /**
         * Incoming Call Target Bearer URI
         *
         * 0x2BBC
         *
         * org.bluetooth.characteristic.incoming_call_target_bearer_uri
         */
        public const val INCOMING_CALL_TARGET_BEARER_URI: UInt = 0x2BBC_u

        /**
         * Call State
         *
         * 0x2BBD
         *
         * org.bluetooth.characteristic.call_state
         */
        public const val CALL_STATE: UInt = 0x2BBD_u

        /**
         * Call Control Point
         *
         * 0x2BBE
         *
         * org.bluetooth.characteristic.call_control_point
         */
        public const val CALL_CONTROL_POINT: UInt = 0x2BBE_u

        /**
         * Call Control Point Optional Opcodes
         *
         * 0x2BBF
         *
         * org.bluetooth.characteristic.call_control_point_optional_opcodes
         */
        public const val CALL_CONTROL_POINT_OPTIONAL_OPCODES: UInt = 0x2BBF_u

        /**
         * Termination Reason
         *
         * 0x2BC0
         *
         * org.bluetooth.characteristic.termination_reason
         */
        public const val TERMINATION_REASON: UInt = 0x2BC0_u

        /**
         * Incoming Call
         *
         * 0x2BC1
         *
         * org.bluetooth.characteristic.incoming_call
         */
        public const val INCOMING_CALL: UInt = 0x2BC1_u

        /**
         * Call Friendly Name
         *
         * 0x2BC2
         *
         * org.bluetooth.characteristic.call_friendly_name
         */
        public const val CALL_FRIENDLY_NAME: UInt = 0x2BC2_u

        /**
         * Mute
         *
         * 0x2BC3
         *
         * org.bluetooth.characteristic.mute
         */
        public const val MUTE: UInt = 0x2BC3_u

        /**
         * Sink ASE
         *
         * 0x2BC4
         *
         * org.bluetooth.characteristic.sink_ase
         */
        public const val SINK_ASE: UInt = 0x2BC4_u

        /**
         * Source ASE
         *
         * 0x2BC5
         *
         * org.bluetooth.characteristic.source_ase
         */
        public const val SOURCE_ASE: UInt = 0x2BC5_u

        /**
         * ASE Control Point
         *
         * 0x2BC6
         *
         * org.bluetooth.characteristic.ase_control_point
         */
        public const val ASE_CONTROL_POINT: UInt = 0x2BC6_u

        /**
         * Broadcast Audio Scan Control Point
         *
         * 0x2BC7
         *
         * org.bluetooth.characteristic.broadcast_audio_scan_control_point
         */
        public const val BROADCAST_AUDIO_SCAN_CONTROL_POINT: UInt = 0x2BC7_u

        /**
         * Broadcast Receive State
         *
         * 0x2BC8
         *
         * org.bluetooth.characteristic.broadcast_receive_state
         */
        public const val BROADCAST_RECEIVE_STATE: UInt = 0x2BC8_u

        /**
         * Sink PAC
         *
         * 0x2BC9
         *
         * org.bluetooth.characteristic.sink_pac
         */
        public const val SINK_PAC: UInt = 0x2BC9_u

        /**
         * Sink Audio Locations
         *
         * 0x2BCA
         *
         * org.bluetooth.characteristic.sink_audio_locations
         */
        public const val SINK_AUDIO_LOCATIONS: UInt = 0x2BCA_u

        /**
         * Source PAC
         *
         * 0x2BCB
         *
         * org.bluetooth.characteristic.source_pac
         */
        public const val SOURCE_PAC: UInt = 0x2BCB_u

        /**
         * Source Audio Locations
         *
         * 0x2BCC
         *
         * org.bluetooth.characteristic.source_audio_locations
         */
        public const val SOURCE_AUDIO_LOCATIONS: UInt = 0x2BCC_u

        /**
         * Available Audio Contexts
         *
         * 0x2BCD
         *
         * org.bluetooth.characteristic.available_audio_contexts
         */
        public const val AVAILABLE_AUDIO_CONTEXTS: UInt = 0x2BCD_u

        /**
         * Supported Audio Contexts
         *
         * 0x2BCE
         *
         * org.bluetooth.characteristic.supported_audio_contexts
         */
        public const val SUPPORTED_AUDIO_CONTEXTS: UInt = 0x2BCE_u

        /**
         * Ammonia Concentration
         *
         * 0x2BCF
         *
         * org.bluetooth.characteristic.ammonia_concentration
         */
        public const val AMMONIA_CONCENTRATION: UInt = 0x2BCF_u

        /**
         * Carbon Monoxide Concentration
         *
         * 0x2BD0
         *
         * org.bluetooth.characteristic.carbon_monoxide_concentration
         */
        public const val CARBON_MONOXIDE_CONCENTRATION: UInt = 0x2BD0_u

        /**
         * Methane Concentration
         *
         * 0x2BD1
         *
         * org.bluetooth.characteristic.methane_concentration
         */
        public const val METHANE_CONCENTRATION: UInt = 0x2BD1_u

        /**
         * Nitrogen Dioxide Concentration
         *
         * 0x2BD2
         *
         * org.bluetooth.characteristic.nitrogen_dioxide_concentration
         */
        public const val NITROGEN_DIOXIDE_CONCENTRATION: UInt = 0x2BD2_u

        /**
         * Non-Methane Volatile Organic Compounds Concentration
         *
         * 0x2BD3
         *
         * org.bluetooth.characteristic.non-methane_volatile_organic_compounds_concentration
         */
        public const val NON_METHANE_VOLATILE_ORGANIC_COMPOUNDS_CONCENTRATION: UInt = 0x2BD3_u

        /**
         * Ozone Concentration
         *
         * 0x2BD4
         *
         * org.bluetooth.characteristic.ozone_concentration
         */
        public const val OZONE_CONCENTRATION: UInt = 0x2BD4_u

        /**
         * Particulate Matter - PM1 Concentration
         *
         * 0x2BD5
         *
         * org.bluetooth.characteristic.particulate_matter_pm1_concentration
         */
        public const val PARTICULATE_MATTER_PM1_CONCENTRATION: UInt = 0x2BD5_u

        /**
         * Particulate Matter - PM2.5 Concentration
         *
         * 0x2BD6
         *
         * org.bluetooth.characteristic.particulate_matter_pm2_5_concentration
         */
        public const val PARTICULATE_MATTER_PM2_5_CONCENTRATION: UInt = 0x2BD6_u

        /**
         * Particulate Matter - PM10 Concentration
         *
         * 0x2BD7
         *
         * org.bluetooth.characteristic.particulate_matter_pm10_concentration
         */
        public const val PARTICULATE_MATTER_PM10_CONCENTRATION: UInt = 0x2BD7_u

        /**
         * Sulfur Dioxide Concentration
         *
         * 0x2BD8
         *
         * org.bluetooth.characteristic.sulfur_dioxide_concentration
         */
        public const val SULFUR_DIOXIDE_CONCENTRATION: UInt = 0x2BD8_u

        /**
         * Sulfur Hexafluoride Concentration
         *
         * 0x2BD9
         *
         * org.bluetooth.characteristic.sulfur_hexafluoride_concentration
         */
        public const val SULFUR_HEXAFLUORIDE_CONCENTRATION: UInt = 0x2BD9_u

        /**
         * Hearing Aid Features
         *
         * 0x2BDA
         *
         * org.bluetooth.characteristic.hearing_aid_features
         */
        public const val HEARING_AID_FEATURES: UInt = 0x2BDA_u

        /**
         * Hearing Aid Preset Control Point
         *
         * 0x2BDB
         *
         * org.bluetooth.characteristic.hearing_aid_preset_control_point
         */
        public const val HEARING_AID_PRESET_CONTROL_POINT: UInt = 0x2BDB_u

        /**
         * Active Preset Index
         *
         * 0x2BDC
         *
         * org.bluetooth.characteristic.active_preset_index
         */
        public const val ACTIVE_PRESET_INDEX: UInt = 0x2BDC_u

        /**
         * Stored Health Observations
         *
         * 0x2BDD
         *
         * org.bluetooth.characteristic.stored_health_observations
         */
        public const val STORED_HEALTH_OBSERVATIONS: UInt = 0x2BDD_u

        /**
         * Fixed String 64
         *
         * 0x2BDE
         *
         * org.bluetooth.characteristic.fixed_string_64
         */
        public const val FIXED_STRING_64: UInt = 0x2BDE_u

        /**
         * High Temperature
         *
         * 0x2BDF
         *
         * org.bluetooth.characteristic.high_temperature
         */
        public const val HIGH_TEMPERATURE: UInt = 0x2BDF_u

        /**
         * High Voltage
         *
         * 0x2BE0
         *
         * org.bluetooth.characteristic.high_voltage
         */
        public const val HIGH_VOLTAGE: UInt = 0x2BE0_u

        /**
         * Light Distribution
         *
         * 0x2BE1
         *
         * org.bluetooth.characteristic.light_distribution
         */
        public const val LIGHT_DISTRIBUTION: UInt = 0x2BE1_u

        /**
         * Light Output
         *
         * 0x2BE2
         *
         * org.bluetooth.characteristic.light_output
         */
        public const val LIGHT_OUTPUT: UInt = 0x2BE2_u

        /**
         * Light Source Type
         *
         * 0x2BE3
         *
         * org.bluetooth.characteristic.light_source_type
         */
        public const val LIGHT_SOURCE_TYPE: UInt = 0x2BE3_u

        /**
         * Noise
         *
         * 0x2BE4
         *
         * org.bluetooth.characteristic.noise
         */
        public const val NOISE: UInt = 0x2BE4_u

        /**
         * Relative Runtime in a Correlated Color Temperature Range
         *
         * 0x2BE5
         *
         * org.bluetooth.characteristic.relative_runtime_in_a_correlated_color_temperature_range
         */
        public const val RELATIVE_RUNTIME_IN_A_CORRELATED_COLOR_TEMPERATURE_RANGE: UInt = 0x2BE5_u

        /**
         * Time Second 32
         *
         * 0x2BE6
         *
         * org.bluetooth.characteristic.time_second_32
         */
        public const val TIME_SECOND_32: UInt = 0x2BE6_u

        /**
         * VOC Concentration
         *
         * 0x2BE7
         *
         * org.bluetooth.characteristic.voc_concentration
         */
        public const val VOC_CONCENTRATION: UInt = 0x2BE7_u

        /**
         * Voltage Frequency
         *
         * 0x2BE8
         *
         * org.bluetooth.characteristic.voltage_frequency
         */
        public const val VOLTAGE_FREQUENCY: UInt = 0x2BE8_u

        /**
         * Battery Critical Status
         *
         * 0x2BE9
         *
         * org.bluetooth.characteristic.battery_critical_status
         */
        public const val BATTERY_CRITICAL_STATUS: UInt = 0x2BE9_u

        /**
         * Battery Health Status
         *
         * 0x2BEA
         *
         * org.bluetooth.characteristic.battery_health_status
         */
        public const val BATTERY_HEALTH_STATUS: UInt = 0x2BEA_u

        /**
         * Battery Health Information
         *
         * 0x2BEB
         *
         * org.bluetooth.characteristic.battery_health_information
         */
        public const val BATTERY_HEALTH_INFORMATION: UInt = 0x2BEB_u

        /**
         * Battery Information
         *
         * 0x2BEC
         *
         * org.bluetooth.characteristic.battery_information
         */
        public const val BATTERY_INFORMATION: UInt = 0x2BEC_u

        /**
         * Battery Level Status
         *
         * 0x2BED
         *
         * org.bluetooth.characteristic.battery_level_status
         */
        public const val BATTERY_LEVEL_STATUS: UInt = 0x2BED_u

        /**
         * Battery Time Status
         *
         * 0x2BEE
         *
         * org.bluetooth.characteristic.battery_time_status
         */
        public const val BATTERY_TIME_STATUS: UInt = 0x2BEE_u

        /**
         * Estimated Service Date
         *
         * 0x2BEF
         *
         * org.bluetooth.characteristic.estimated_service_date
         */
        public const val ESTIMATED_SERVICE_DATE: UInt = 0x2BEF_u

        /**
         * Battery Energy Status
         *
         * 0x2BF0
         *
         * org.bluetooth.characteristic.battery_energy_status
         */
        public const val BATTERY_ENERGY_STATUS: UInt = 0x2BF0_u

        /**
         * Observation Schedule Changed
         *
         * 0x2BF1
         *
         * org.bluetooth.characteristic.observation_schedule_changed
         */
        public const val OBSERVATION_SCHEDULE_CHANGED: UInt = 0x2BF1_u

        /**
         * Current Elapsed Time
         *
         * 0x2BF2
         *
         * org.bluetooth.characteristic.current_elapsed_time
         */
        public const val CURRENT_ELAPSED_TIME: UInt = 0x2BF2_u

        /**
         * Health Sensor Features
         *
         * 0x2BF3
         *
         * org.bluetooth.characteristic.health_sensor_features
         */
        public const val HEALTH_SENSOR_FEATURES: UInt = 0x2BF3_u

        /**
         * GHS Control Point
         *
         * 0x2BF4
         *
         * org.bluetooth.characteristic.ghs_control_point
         */
        public const val GHS_CONTROL_POINT: UInt = 0x2BF4_u

        /**
         * LE GATT Security Levels
         *
         * 0x2BF5
         *
         * org.bluetooth.characteristic.le_gatt_security_levels
         */
        public const val LE_GATT_SECURITY_LEVELS: UInt = 0x2BF5_u

        /**
         * ESL Address
         *
         * 0x2BF6
         *
         * org.bluetooth.characteristic.esl_address
         */
        public const val ESL_ADDRESS: UInt = 0x2BF6_u

        /**
         * AP Sync Key Material
         *
         * 0x2BF7
         *
         * org.bluetooth.characteristic.ap_sync_key_material
         */
        public const val AP_SYNC_KEY_MATERIAL: UInt = 0x2BF7_u

        /**
         * ESL Response Key Material
         *
         * 0x2BF8
         *
         * org.bluetooth.characteristic.esl_response_key_material
         */
        public const val ESL_RESPONSE_KEY_MATERIAL: UInt = 0x2BF8_u

        /**
         * ESL Current Absolute Time
         *
         * 0x2BF9
         *
         * org.bluetooth.characteristic.esl_current_absolute_time
         */
        public const val ESL_CURRENT_ABSOLUTE_TIME: UInt = 0x2BF9_u

        /**
         * ESL Display Information
         *
         * 0x2BFA
         *
         * org.bluetooth.characteristic.esl_display_information
         */
        public const val ESL_DISPLAY_INFORMATION: UInt = 0x2BFA_u

        /**
         * ESL Image Information
         *
         * 0x2BFB
         *
         * org.bluetooth.characteristic.esl_image_information
         */
        public const val ESL_IMAGE_INFORMATION: UInt = 0x2BFB_u

        /**
         * ESL Sensor Information
         *
         * 0x2BFC
         *
         * org.bluetooth.characteristic.esl_sensor_information
         */
        public const val ESL_SENSOR_INFORMATION: UInt = 0x2BFC_u

        /**
         * ESL LED Information
         *
         * 0x2BFD
         *
         * org.bluetooth.characteristic.esl_led_information
         */
        public const val ESL_LED_INFORMATION: UInt = 0x2BFD_u

        /**
         * ESL Control Point
         *
         * 0x2BFE
         *
         * org.bluetooth.characteristic.esl_control_point
         */
        public const val ESL_CONTROL_POINT: UInt = 0x2BFE_u

        /**
         * UDI for Medical Devices
         *
         * 0x2BFF
         *
         * org.bluetooth.characteristic.medical_devices
         */
        public const val UDI_FOR_MEDICAL_DEVICES: UInt = 0x2BFF_u

        /**
         * GMAP Role
         *
         * 0x2C00
         *
         * org.bluetooth.characteristic.gmap_role
         */
        public const val GMAP_ROLE: UInt = 0x2C00_u

        /**
         * UGG Features
         *
         * 0x2C01
         *
         * org.bluetooth.characteristic.ugg_features
         */
        public const val UGG_FEATURES: UInt = 0x2C01_u

        /**
         * UGT Features
         *
         * 0x2C02
         *
         * org.bluetooth.characteristic.ugt_features
         */
        public const val UGT_FEATURES: UInt = 0x2C02_u

        /**
         * BGS Features
         *
         * 0x2C03
         *
         * org.bluetooth.characteristic.bgs_features
         */
        public const val BGS_FEATURES: UInt = 0x2C03_u

        /**
         * BGR Features
         *
         * 0x2C04
         *
         * org.bluetooth.characteristic.bgr_features
         */
        public const val BGR_FEATURES: UInt = 0x2C04_u

        /**
         * Percentage 8 Steps
         *
         * 0x2C05
         *
         * org.bluetooth.characteristic.percentage_8_steps
         */
        public const val PERCENTAGE_8_STEPS: UInt = 0x2C05_u

        /**
         * Acceleration
         *
         * 0x2C06
         *
         * org.bluetooth.characteristic.acceleration
         */
        public const val ACCELERATION: UInt = 0x2C06_u

        /**
         * Force
         *
         * 0x2C07
         *
         * org.bluetooth.characteristic.force
         */
        public const val FORCE: UInt = 0x2C07_u

        /**
         * Linear Position
         *
         * 0x2C08
         *
         * org.bluetooth.characteristic.linear_position
         */
        public const val LINEAR_POSITION: UInt = 0x2C08_u

        /**
         * Rotational Speed
         *
         * 0x2C09
         *
         * org.bluetooth.characteristic.rotational_speed
         */
        public const val ROTATIONAL_SPEED: UInt = 0x2C09_u

        /**
         * Length
         *
         * 0x2C0A
         *
         * org.bluetooth.characteristic.length
         */
        public const val LENGTH: UInt = 0x2C0A_u

        /**
         * Torque
         *
         * 0x2C0B
         *
         * org.bluetooth.characteristic.torque
         */
        public const val TORQUE: UInt = 0x2C0B_u

        /**
         * IMD Status
         *
         * 0x2C0C
         *
         * org.bluetooth.characteristic.imd_status
         */
        public const val IMD_STATUS: UInt = 0x2C0C_u

        /**
         * IMDS Descriptor Value Changed
         *
         * 0x2C0D
         *
         * org.bluetooth.characteristic.imds_descriptor_value_changed
         */
        public const val IMDS_DESCRIPTOR_VALUE_CHANGED: UInt = 0x2C0D_u

        /**
         * First Use Date
         *
         * 0x2C0E
         *
         * org.bluetooth.characteristic.first_use_date
         */
        public const val FIRST_USE_DATE: UInt = 0x2C0E_u

        /**
         * Life Cycle Data
         *
         * 0x2C0F
         *
         * org.bluetooth.characteristic.life_cycle_data
         */
        public const val LIFE_CYCLE_DATA: UInt = 0x2C0F_u

        /**
         * Work Cycle Data
         *
         * 0x2C10
         *
         * org.bluetooth.characteristic.work_cycle_data
         */
        public const val WORK_CYCLE_DATA: UInt = 0x2C10_u

        /**
         * Service Cycle Data
         *
         * 0x2C11
         *
         * org.bluetooth.characteristic.service_cycle_data
         */
        public const val SERVICE_CYCLE_DATA: UInt = 0x2C11_u

        /**
         * IMD Control
         *
         * 0x2C12
         *
         * org.bluetooth.characteristic.imd_control
         */
        public const val IMD_CONTROL: UInt = 0x2C12_u

        /**
         * IMD Historical Data
         *
         * 0x2C13
         *
         * org.bluetooth.characteristic.imd_historical_data
         */
        public const val IMD_HISTORICAL_DATA: UInt = 0x2C13_u

        /**
         * RAS Features
         *
         * 0x2C14
         *
         * org.bluetooth.characteristic.ras_features
         */
        public const val RAS_FEATURES: UInt = 0x2C14_u

        /**
         * Real-time Ranging Data
         *
         * 0x2C15
         *
         * org.bluetooth.characteristic.real-time_ranging_data
         */
        public const val REAL_TIME_RANGING_DATA: UInt = 0x2C15_u

        /**
         * On-demand Ranging Data
         *
         * 0x2C16
         *
         * org.bluetooth.characteristic.on-demand_ranging_data
         */
        public const val ON_DEMAND_RANGING_DATA: UInt = 0x2C16_u

        /**
         * RAS Control Point
         *
         * 0x2C17
         *
         * org.bluetooth.characteristic.ras_control_point
         */
        public const val RAS_CONTROL_POINT: UInt = 0x2C17_u

        /**
         * Ranging Data Ready
         *
         * 0x2C18
         *
         * org.bluetooth.characteristic.ranging_data_ready
         */
        public const val RANGING_DATA_READY: UInt = 0x2C18_u

        /**
         * Ranging Data Overwritten
         *
         * 0x2C19
         *
         * org.bluetooth.characteristic.ranging_data_overwritten
         */
        public const val RANGING_DATA_OVERWRITTEN: UInt = 0x2C19_u
    }

    /**
     * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/uuids/descriptors.yaml
     */
    public object Descriptors {
        /**
         * Characteristic Extended Properties
         *
         * 0x2900
         *
         * org.bluetooth.descriptor.gatt.characteristic_extended_properties
         */
        public const val CHARACTERISTIC_EXTENDED_PROPERTIES: UInt = 0x2900_u

        /**
         * Characteristic User Description
         *
         * 0x2901
         *
         * org.bluetooth.descriptor.gatt.characteristic_user_description
         */
        public const val CHARACTERISTIC_USER_DESCRIPTION: UInt = 0x2901_u

        /**
         * Client Characteristic Configuration
         *
         * 0x2902
         *
         * org.bluetooth.descriptor.gatt.client_characteristic_configuration
         */
        public const val CLIENT_CHARACTERISTIC_CONFIGURATION: UInt = 0x2902_u

        /**
         * Server Characteristic Configuration
         *
         * 0x2903
         *
         * org.bluetooth.descriptor.gatt.server_characteristic_configuration
         */
        public const val SERVER_CHARACTERISTIC_CONFIGURATION: UInt = 0x2903_u

        /**
         * Characteristic Presentation Format
         *
         * 0x2904
         *
         * org.bluetooth.descriptor.gatt.characteristic_presentation_format
         */
        public const val CHARACTERISTIC_PRESENTATION_FORMAT: UInt = 0x2904_u

        /**
         * Characteristic Aggregate Format
         *
         * 0x2905
         *
         * org.bluetooth.descriptor.gatt.characteristic_aggregate_format
         */
        public const val CHARACTERISTIC_AGGREGATE_FORMAT: UInt = 0x2905_u

        /**
         * Valid Range
         *
         * 0x2906
         *
         * org.bluetooth.descriptor.valid_range
         */
        public const val VALID_RANGE: UInt = 0x2906_u

        /**
         * External Report Reference
         *
         * 0x2907
         *
         * org.bluetooth.descriptor.external_report_reference
         */
        public const val EXTERNAL_REPORT_REFERENCE: UInt = 0x2907_u

        /**
         * Report Reference
         *
         * 0x2908
         *
         * org.bluetooth.descriptor.report_reference
         */
        public const val REPORT_REFERENCE: UInt = 0x2908_u

        /**
         * Number of Digitals
         *
         * 0x2909
         *
         * org.bluetooth.descriptor.number_of_digitals
         */
        public const val NUMBER_OF_DIGITALS: UInt = 0x2909_u

        /**
         * Value Trigger Setting
         *
         * 0x290A
         *
         * org.bluetooth.descriptor.value_trigger_setting
         */
        public const val VALUE_TRIGGER_SETTING: UInt = 0x290A_u

        /**
         * Environmental Sensing Configuration
         *
         * 0x290B
         *
         * org.bluetooth.descriptor.es_configuration
         */
        public const val ENVIRONMENTAL_SENSING_CONFIGURATION: UInt = 0x290B_u

        /**
         * Environmental Sensing Measurement
         *
         * 0x290C
         *
         * org.bluetooth.descriptor.es_measurement
         */
        public const val ENVIRONMENTAL_SENSING_MEASUREMENT: UInt = 0x290C_u

        /**
         * Environmental Sensing Trigger Setting
         *
         * 0x290D
         *
         * org.bluetooth.descriptor.es_trigger_setting
         */
        public const val ENVIRONMENTAL_SENSING_TRIGGER_SETTING: UInt = 0x290D_u

        /**
         * Time Trigger Setting
         *
         * 0x290E
         *
         * org.bluetooth.descriptor.time_trigger_setting
         */
        public const val TIME_TRIGGER_SETTING: UInt = 0x290E_u

        /**
         * Complete BR-EDR Transport Block Data
         *
         * 0x290F
         *
         * org.bluetooth.descriptor.complete_br_edr_transport_block_data
         */
        public const val COMPLETE_BR_EDR_TRANSPORT_BLOCK_DATA: UInt = 0x290F_u

        /**
         * Observation Schedule
         *
         * 0x2910
         *
         * org.bluetooth.descriptor.observation_schedule
         */
        public const val OBSERVATION_SCHEDULE: UInt = 0x2910_u

        /**
         * Valid Range and Accuracy
         *
         * 0x2911
         *
         * org.bluetooth.descriptor.valid_range_accuracy
         */
        public const val VALID_RANGE_AND_ACCURACY: UInt = 0x2911_u

        /**
         * Measurement Description
         *
         * 0x2912
         *
         * org.bluetooth.descriptor.measurement_description
         */
        public const val MEASUREMENT_DESCRIPTION: UInt = 0x2912_u

        /**
         * Manufacturer Limits
         *
         * 0x2913
         *
         * org.bluetooth.descriptor.manufacturer_limits
         */
        public const val MANUFACTURER_LIMITS: UInt = 0x2913_u

        /**
         * Process Tolerances
         *
         * 0x2914
         *
         * org.bluetooth.descriptor.process_tolerances
         */
        public const val PROCESS_TOLERANCES: UInt = 0x2914_u

        /**
         * IMD Trigger Setting
         *
         * 0x2915
         *
         * org.bluetooth.descriptor.imd_trigger_setting
         */
        public const val IMD_TRIGGER_SETTING: UInt = 0x2915_u
    }
}
