// Bluetooth SIG assigned numbers sourced from:
// - https://github.com/chungchungdev/kotlin-bluetoothsig-assigned-numbers/blob/master/generated/AssignedNumbers.kt @ 2616a854ccf80fa8034dccebbef206b9d99fad40
//   Copyright chungchungdev, used under the Apache License, Version 2.0.
//
// https://www.bluetooth.com/specifications/assigned-numbers/
//
// Last update: 2025-05-05 (UTC+0)

@file:Suppress("ktlint:standard:class-naming")

package org.bluetooth

/**
 * Bluetooth SIG assigned numbers for services.
 *
 * Constant names exactly match the IDs defined in the Bluetooth SIG assigned numbers YAML file
 * (e.g. `org.bluetooth.service.heart_rate`). Combine with `Bluetooth.BaseUuid` (from
 * `kable-core`) to produce a full 128-bit [Uuid][kotlin.uuid.Uuid], for example:
 *
 * ```
 * val heartRateServiceUuid = Bluetooth.BaseUuid + org.bluetooth.service.heart_rate
 * println(heartRateServiceUuid) // Output: 0000180d-0000-1000-8000-00805f9b34fb
 * ```
 *
 * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/uuids/service_uuids.yaml
 */
public object service {
    /** GAP */
    public const val gap: UInt = 0x1800_u

    /** GATT */
    public const val gatt: UInt = 0x1801_u

    /** Immediate Alert */
    public const val immediate_alert: UInt = 0x1802_u

    /** Link Loss */
    public const val link_loss: UInt = 0x1803_u

    /** Tx Power */
    public const val tx_power: UInt = 0x1804_u

    /** Current Time */
    public const val current_time: UInt = 0x1805_u

    /** Reference Time Update */
    public const val reference_time_update: UInt = 0x1806_u

    /** Next DST Change */
    public const val next_dst_change: UInt = 0x1807_u

    /** Glucose */
    public const val glucose: UInt = 0x1808_u

    /** Health Thermometer */
    public const val health_thermometer: UInt = 0x1809_u

    /** Device Information */
    public const val device_information: UInt = 0x180A_u

    /** Heart Rate */
    public const val heart_rate: UInt = 0x180D_u

    /** Phone Alert Status */
    public const val phone_alert_status: UInt = 0x180E_u

    /** Battery */
    public const val battery_service: UInt = 0x180F_u

    /** Blood Pressure */
    public const val blood_pressure: UInt = 0x1810_u

    /** Alert Notification */
    public const val alert_notification: UInt = 0x1811_u

    /** Human Interface Device */
    public const val human_interface_device: UInt = 0x1812_u

    /** Scan Parameters */
    public const val scan_parameters: UInt = 0x1813_u

    /** Running Speed and Cadence */
    public const val running_speed_and_cadence: UInt = 0x1814_u

    /** Automation IO */
    public const val automation_io: UInt = 0x1815_u

    /** Cycling Speed and Cadence */
    public const val cycling_speed_and_cadence: UInt = 0x1816_u

    /** Cycling Power */
    public const val cycling_power: UInt = 0x1818_u

    /** Location and Navigation */
    public const val location_and_navigation: UInt = 0x1819_u

    /** Environmental Sensing */
    public const val environmental_sensing: UInt = 0x181A_u

    /** Body Composition */
    public const val body_composition: UInt = 0x181B_u

    /** User Data */
    public const val user_data: UInt = 0x181C_u

    /** Weight Scale */
    public const val weight_scale: UInt = 0x181D_u

    /** Bond Management */
    public const val bond_management: UInt = 0x181E_u

    /** Continuous Glucose Monitoring */
    public const val continuous_glucose_monitoring: UInt = 0x181F_u

    /** Internet Protocol Support */
    public const val internet_protocol_support: UInt = 0x1820_u

    /** Indoor Positioning */
    public const val indoor_positioning: UInt = 0x1821_u

    /** Pulse Oximeter */
    public const val pulse_oximeter: UInt = 0x1822_u

    /** HTTP Proxy */
    public const val http_proxy: UInt = 0x1823_u

    /** Transport Discovery */
    public const val transport_discovery: UInt = 0x1824_u

    /** Object Transfer */
    public const val object_transfer: UInt = 0x1825_u

    /** Fitness Machine */
    public const val fitness_machine: UInt = 0x1826_u

    /** Mesh Provisioning */
    public const val mesh_provisioning: UInt = 0x1827_u

    /** Mesh Proxy */
    public const val mesh_proxy: UInt = 0x1828_u

    /** Reconnection Configuration */
    public const val reconnection_configuration: UInt = 0x1829_u

    /** Insulin Delivery */
    public const val insulin_delivery: UInt = 0x183A_u

    /** Binary Sensor */
    public const val binary_sensor: UInt = 0x183B_u

    /** Emergency Configuration */
    public const val emergency_configuration: UInt = 0x183C_u

    /** Authorization Control */
    public const val authorization_control: UInt = 0x183D_u

    /** Physical Activity Monitor */
    public const val physical_activity_monitor: UInt = 0x183E_u

    /** Elapsed Time */
    public const val elapsed_time: UInt = 0x183F_u

    /** Generic Health Sensor */
    public const val generic_health_sensor: UInt = 0x1840_u

    /** Audio Input Control */
    public const val audio_input_control: UInt = 0x1843_u

    /** Volume Control */
    public const val volume_control: UInt = 0x1844_u

    /** Volume Offset Control */
    public const val volume_offset: UInt = 0x1845_u

    /** Coordinated Set Identification */
    public const val coordinated_set_identification: UInt = 0x1846_u

    /** Device Time */
    public const val device_time: UInt = 0x1847_u

    /** Media Control */
    public const val media_control: UInt = 0x1848_u

    /** Generic Media Control */
    public const val generic_media_control: UInt = 0x1849_u

    /** Constant Tone Extension */
    public const val constant_tone_extension: UInt = 0x184A_u

    /** Telephone Bearer */
    public const val telephone_bearer: UInt = 0x184B_u

    /** Generic Telephone Bearer */
    public const val generic_telephone_bearer: UInt = 0x184C_u

    /** Microphone Control */
    public const val microphone_control: UInt = 0x184D_u

    /** Audio Stream Control */
    public const val audio_stream_control: UInt = 0x184E_u

    /** Broadcast Audio Scan */
    public const val broadcast_audio_scan: UInt = 0x184F_u

    /** Published Audio Capabilities */
    public const val published_audio_capabilities: UInt = 0x1850_u

    /** Basic Audio Announcement */
    public const val basic_audio_announcement: UInt = 0x1851_u

    /** Broadcast Audio Announcement */
    public const val broadcast_audio_announcement: UInt = 0x1852_u

    /** Common Audio */
    public const val common_audio: UInt = 0x1853_u

    /** Hearing Access */
    public const val hearing_access: UInt = 0x1854_u

    /** Telephony and Media Audio */
    public const val telephony_and_media_audio: UInt = 0x1855_u

    /** Public Broadcast Announcement */
    public const val public_broadcast_announcement: UInt = 0x1856_u

    /** Electronic Shelf Label */
    public const val electronic_shelf_label: UInt = 0x1857_u

    /** Gaming Audio */
    public const val gaming_audio: UInt = 0x1858_u

    /** Mesh Proxy Solicitation */
    public const val mesh_proxy_solicitation: UInt = 0x1859_u

    /** Industrial Measurement Device */
    public const val industrial_measurement_device: UInt = 0x185A_u

    /** Ranging */
    public const val ranging: UInt = 0x185B_u
}

/**
 * Bluetooth SIG assigned numbers for characteristics.
 *
 * Constant names exactly match the IDs defined in the Bluetooth SIG assigned numbers YAML file
 * (e.g. `org.bluetooth.characteristic.heart_rate_measurement`).
 *
 * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/uuids/characteristic_uuids.yaml
 */
public object characteristic {
    public object gap {
        /** Device Name */
        public const val device_name: UInt = 0x2A00_u

        /** Appearance */
        public const val appearance: UInt = 0x2A01_u

        /** Peripheral Privacy Flag */
        public const val peripheral_privacy_flag: UInt = 0x2A02_u

        /** Reconnection Address */
        public const val reconnection_address: UInt = 0x2A03_u

        /** Peripheral Preferred Connection Parameters */
        public const val peripheral_preferred_connection_parameters: UInt = 0x2A04_u

        /** Central Address Resolution */
        public const val central_address_resolution: UInt = 0x2AA6_u
    }

    public object gatt {
        /** Service Changed */
        public const val service_changed: UInt = 0x2A05_u
    }

    /** Alert Level */
    public const val alert_level: UInt = 0x2A06_u

    /** Tx Power Level */
    public const val tx_power_level: UInt = 0x2A07_u

    /** Date Time */
    public const val date_time: UInt = 0x2A08_u

    /** Day of Week */
    public const val day_of_week: UInt = 0x2A09_u

    /** Day Date Time */
    public const val day_date_time: UInt = 0x2A0A_u

    /** Exact Time 256 */
    public const val exact_time_256: UInt = 0x2A0C_u

    /** DST Offset */
    public const val dst_offset: UInt = 0x2A0D_u

    /** Time Zone */
    public const val time_zone: UInt = 0x2A0E_u

    /** Local Time Information */
    public const val local_time_information: UInt = 0x2A0F_u

    /** Time with DST */
    public const val time_with_dst: UInt = 0x2A11_u

    /** Time Accuracy */
    public const val time_accuracy: UInt = 0x2A12_u

    /** Time Source */
    public const val time_source: UInt = 0x2A13_u

    /** Reference Time Information */
    public const val reference_time_information: UInt = 0x2A14_u

    /** Time Update Control Point */
    public const val time_update_control_point: UInt = 0x2A16_u

    /** Time Update State */
    public const val time_update_state: UInt = 0x2A17_u

    /** Glucose Measurement */
    public const val glucose_measurement: UInt = 0x2A18_u

    /** Battery Level */
    public const val battery_level: UInt = 0x2A19_u

    /** Temperature Measurement */
    public const val temperature_measurement: UInt = 0x2A1C_u

    /** Temperature Type */
    public const val temperature_type: UInt = 0x2A1D_u

    /** Intermediate Temperature */
    public const val intermediate_temperature: UInt = 0x2A1E_u

    /** Measurement Interval */
    public const val measurement_interval: UInt = 0x2A21_u

    /** Boot Keyboard Input Report */
    public const val boot_keyboard_input_report: UInt = 0x2A22_u

    /** System ID */
    public const val system_id: UInt = 0x2A23_u

    /** Model Number String */
    public const val model_number_string: UInt = 0x2A24_u

    /** Serial Number String */
    public const val serial_number_string: UInt = 0x2A25_u

    /** Firmware Revision String */
    public const val firmware_revision_string: UInt = 0x2A26_u

    /** Hardware Revision String */
    public const val hardware_revision_string: UInt = 0x2A27_u

    /** Software Revision String */
    public const val software_revision_string: UInt = 0x2A28_u

    /** Manufacturer Name String */
    public const val manufacturer_name_string: UInt = 0x2A29_u

    /** IEEE 11073-20601 Regulatory Certification Data List */
    public const val `ieee_11073-20601_regulatory_certification_data_list`: UInt = 0x2A2A_u

    /** Current Time */
    public const val current_time: UInt = 0x2A2B_u

    /** Magnetic Declination */
    public const val magnetic_declination: UInt = 0x2A2C_u

    /** Scan Refresh */
    public const val scan_refresh: UInt = 0x2A31_u

    /** Boot Keyboard Output Report */
    public const val boot_keyboard_output_report: UInt = 0x2A32_u

    /** Boot Mouse Input Report */
    public const val boot_mouse_input_report: UInt = 0x2A33_u

    /** Glucose Measurement Context */
    public const val glucose_measurement_context: UInt = 0x2A34_u

    /** Blood Pressure Measurement */
    public const val blood_pressure_measurement: UInt = 0x2A35_u

    /** Intermediate Cuff Pressure */
    public const val intermediate_cuff_pressure: UInt = 0x2A36_u

    /** Heart Rate Measurement */
    public const val heart_rate_measurement: UInt = 0x2A37_u

    /** Body Sensor Location */
    public const val body_sensor_location: UInt = 0x2A38_u

    /** Heart Rate Control Point */
    public const val heart_rate_control_point: UInt = 0x2A39_u

    /** Alert Status */
    public const val alert_status: UInt = 0x2A3F_u

    /** Ringer Control Point */
    public const val ringer_control_point: UInt = 0x2A40_u

    /** Ringer Setting */
    public const val ringer_setting: UInt = 0x2A41_u

    /** Alert Category ID Bit Mask */
    public const val alert_category_id_bit_mask: UInt = 0x2A42_u

    /** Alert Category ID */
    public const val alert_category_id: UInt = 0x2A43_u

    /** Alert Notification Control Point */
    public const val alert_notification_control_point: UInt = 0x2A44_u

    /** Unread Alert Status */
    public const val unread_alert_status: UInt = 0x2A45_u

    /** New Alert */
    public const val new_alert: UInt = 0x2A46_u

    /** Supported New Alert Category */
    public const val supported_new_alert_category: UInt = 0x2A47_u

    /** Supported Unread Alert Category */
    public const val supported_unread_alert_category: UInt = 0x2A48_u

    /** Blood Pressure Feature */
    public const val blood_pressure_feature: UInt = 0x2A49_u

    /** HID Information */
    public const val hid_information: UInt = 0x2A4A_u

    /** Report Map */
    public const val report_map: UInt = 0x2A4B_u

    /** HID Control Point */
    public const val hid_control_point: UInt = 0x2A4C_u

    /** Report */
    public const val report: UInt = 0x2A4D_u

    /** Protocol Mode */
    public const val protocol_mode: UInt = 0x2A4E_u

    /** Scan Interval Window */
    public const val scan_interval_window: UInt = 0x2A4F_u

    /** PnP ID */
    public const val pnp_id: UInt = 0x2A50_u

    /** Glucose Feature */
    public const val glucose_feature: UInt = 0x2A51_u

    /** Record Access Control Point */
    public const val record_access_control_point: UInt = 0x2A52_u

    /** RSC Measurement */
    public const val rsc_measurement: UInt = 0x2A53_u

    /** RSC Feature */
    public const val rsc_feature: UInt = 0x2A54_u

    /** SC Control Point */
    public const val sc_control_point: UInt = 0x2A55_u

    /** Aggregate */
    public const val aggregate: UInt = 0x2A5A_u

    /** CSC Measurement */
    public const val csc_measurement: UInt = 0x2A5B_u

    /** CSC Feature */
    public const val csc_feature: UInt = 0x2A5C_u

    /** Sensor Location */
    public const val sensor_location: UInt = 0x2A5D_u

    /** PLX Spot-Check Measurement */
    public const val plx_spot_check_measurement: UInt = 0x2A5E_u

    /** PLX Continuous Measurement */
    public const val plx_continuous_measurement: UInt = 0x2A5F_u

    /** PLX Features */
    public const val plx_features: UInt = 0x2A60_u

    /** Cycling Power Measurement */
    public const val cycling_power_measurement: UInt = 0x2A63_u

    /** Cycling Power Vector */
    public const val cycling_power_vector: UInt = 0x2A64_u

    /** Cycling Power Feature */
    public const val cycling_power_feature: UInt = 0x2A65_u

    /** Cycling Power Control Point */
    public const val cycling_power_control_point: UInt = 0x2A66_u

    /** Location and Speed */
    public const val location_and_speed: UInt = 0x2A67_u

    /** Navigation */
    public const val navigation: UInt = 0x2A68_u

    /** Position Quality */
    public const val position_quality: UInt = 0x2A69_u

    /** LN Feature */
    public const val ln_feature: UInt = 0x2A6A_u

    /** LN Control Point */
    public const val ln_control_point: UInt = 0x2A6B_u

    /** Elevation */
    public const val elevation: UInt = 0x2A6C_u

    /** Pressure */
    public const val pressure: UInt = 0x2A6D_u

    /** Temperature */
    public const val temperature: UInt = 0x2A6E_u

    /** Humidity */
    public const val humidity: UInt = 0x2A6F_u

    /** True Wind Speed */
    public const val true_wind_speed: UInt = 0x2A70_u

    /** True Wind Direction */
    public const val true_wind_direction: UInt = 0x2A71_u

    /** Apparent Wind Speed */
    public const val apparent_wind_speed: UInt = 0x2A72_u

    /** Apparent Wind Direction */
    public const val apparent_wind_direction: UInt = 0x2A73_u

    /** Gust Factor */
    public const val gust_factor: UInt = 0x2A74_u

    /** Pollen Concentration */
    public const val pollen_concentration: UInt = 0x2A75_u

    /** UV Index */
    public const val uv_index: UInt = 0x2A76_u

    /** Irradiance */
    public const val irradiance: UInt = 0x2A77_u

    /** Rainfall */
    public const val rainfall: UInt = 0x2A78_u

    /** Wind Chill */
    public const val wind_chill: UInt = 0x2A79_u

    /** Heat Index */
    public const val heat_index: UInt = 0x2A7A_u

    /** Dew Point */
    public const val dew_point: UInt = 0x2A7B_u

    /** Descriptor Value Changed */
    public const val descriptor_value_changed: UInt = 0x2A7D_u

    /** Aerobic Heart Rate Lower Limit */
    public const val aerobic_heart_rate_lower_limit: UInt = 0x2A7E_u

    /** Aerobic Threshold */
    public const val aerobic_threshold: UInt = 0x2A7F_u

    /** Age */
    public const val age: UInt = 0x2A80_u

    /** Anaerobic Heart Rate Lower Limit */
    public const val anaerobic_heart_rate_lower_limit: UInt = 0x2A81_u

    /** Anaerobic Heart Rate Upper Limit */
    public const val anaerobic_heart_rate_upper_limit: UInt = 0x2A82_u

    /** Anaerobic Threshold */
    public const val anaerobic_threshold: UInt = 0x2A83_u

    /** Aerobic Heart Rate Upper Limit */
    public const val aerobic_heart_rate_upper_limit: UInt = 0x2A84_u

    /** Date of Birth */
    public const val date_of_birth: UInt = 0x2A85_u

    /** Date of Threshold Assessment */
    public const val date_of_threshold_assessment: UInt = 0x2A86_u

    /** Email Address */
    public const val email_address: UInt = 0x2A87_u

    /** Fat Burn Heart Rate Lower Limit */
    public const val fat_burn_heart_rate_lower_limit: UInt = 0x2A88_u

    /** Fat Burn Heart Rate Upper Limit */
    public const val fat_burn_heart_rate_upper_limit: UInt = 0x2A89_u

    /** First Name */
    public const val first_name: UInt = 0x2A8A_u

    /** Five Zone Heart Rate Limits */
    public const val five_zone_heart_rate_limits: UInt = 0x2A8B_u

    /** Gender */
    public const val gender: UInt = 0x2A8C_u

    /** Heart Rate Max */
    public const val heart_rate_max: UInt = 0x2A8D_u

    /** Height */
    public const val height: UInt = 0x2A8E_u

    /** Hip Circumference */
    public const val hip_circumference: UInt = 0x2A8F_u

    /** Last Name */
    public const val last_name: UInt = 0x2A90_u

    /** Maximum Recommended Heart Rate */
    public const val maximum_recommended_heart_rate: UInt = 0x2A91_u

    /** Resting Heart Rate */
    public const val resting_heart_rate: UInt = 0x2A92_u

    /** Sport Type for Aerobic and Anaerobic Thresholds */
    public const val sport_type_for_aerobic_and_anaerobic_thresholds: UInt = 0x2A93_u

    /** Three Zone Heart Rate Limits */
    public const val three_zone_heart_rate_limits: UInt = 0x2A94_u

    /** Two Zone Heart Rate Limits */
    public const val two_zone_heart_rate_limits: UInt = 0x2A95_u

    /** VO2 Max */
    public const val vo2_max: UInt = 0x2A96_u

    /** Waist Circumference */
    public const val waist_circumference: UInt = 0x2A97_u

    /** Weight */
    public const val weight: UInt = 0x2A98_u

    /** Database Change Increment */
    public const val database_change_increment: UInt = 0x2A99_u

    /** User Index */
    public const val user_index: UInt = 0x2A9A_u

    /** Body Composition Feature */
    public const val body_composition_feature: UInt = 0x2A9B_u

    /** Body Composition Measurement */
    public const val body_composition_measurement: UInt = 0x2A9C_u

    /** Weight Measurement */
    public const val weight_measurement: UInt = 0x2A9D_u

    /** Weight Scale Feature */
    public const val weight_scale_feature: UInt = 0x2A9E_u

    /** User Control Point */
    public const val user_control_point: UInt = 0x2A9F_u

    /** Magnetic Flux Density - 2D */
    public const val magnetic_flux_density_2d: UInt = 0x2AA0_u

    /** Magnetic Flux Density - 3D */
    public const val magnetic_flux_density_3d: UInt = 0x2AA1_u

    /** Language */
    public const val language: UInt = 0x2AA2_u

    /** Barometric Pressure Trend */
    public const val barometric_pressure_trend: UInt = 0x2AA3_u

    /** Bond Management Control Point */
    public const val bond_management_control_point: UInt = 0x2AA4_u

    /** Bond Management Feature */
    public const val bond_management_feature: UInt = 0x2AA5_u

    /** CGM Measurement */
    public const val cgm_measurement: UInt = 0x2AA7_u

    /** CGM Feature */
    public const val cgm_feature: UInt = 0x2AA8_u

    /** CGM Status */
    public const val cgm_status: UInt = 0x2AA9_u

    /** CGM Session Start Time */
    public const val cgm_session_start_time: UInt = 0x2AAA_u

    /** CGM Session Run Time */
    public const val cgm_session_run_time: UInt = 0x2AAB_u

    /** CGM Specific Ops Control Point */
    public const val cgm_specific_ops_control_point: UInt = 0x2AAC_u

    /** Indoor Positioning Configuration */
    public const val indoor_positioning_configuration: UInt = 0x2AAD_u

    /** Latitude */
    public const val latitude: UInt = 0x2AAE_u

    /** Longitude */
    public const val longitude: UInt = 0x2AAF_u

    /** Local North Coordinate */
    public const val local_north_coordinate: UInt = 0x2AB0_u

    /** Local East Coordinate */
    public const val local_east_coordinate: UInt = 0x2AB1_u

    /** Floor Number */
    public const val floor_number: UInt = 0x2AB2_u

    /** Altitude */
    public const val altitude: UInt = 0x2AB3_u

    /** Uncertainty */
    public const val uncertainty: UInt = 0x2AB4_u

    /** Location Name */
    public const val location_name: UInt = 0x2AB5_u

    /** URI */
    public const val uri: UInt = 0x2AB6_u

    /** HTTP Headers */
    public const val http_headers: UInt = 0x2AB7_u

    /** HTTP Status Code */
    public const val http_status_code: UInt = 0x2AB8_u

    /** HTTP Entity Body */
    public const val http_entity_body: UInt = 0x2AB9_u

    /** HTTP Control Point */
    public const val http_control_point: UInt = 0x2ABA_u

    /** HTTPS Security */
    public const val https_security: UInt = 0x2ABB_u

    /** TDS Control Point */
    public const val tds_control_point: UInt = 0x2ABC_u

    /** OTS Feature */
    public const val ots_feature: UInt = 0x2ABD_u

    /** Object Name */
    public const val object_name: UInt = 0x2ABE_u

    /** Object Type */
    public const val object_type: UInt = 0x2ABF_u

    /** Object Size */
    public const val object_size: UInt = 0x2AC0_u

    /** Object First-Created */
    public const val object_first_created: UInt = 0x2AC1_u

    /** Object Last-Modified */
    public const val object_last_modified: UInt = 0x2AC2_u

    /** Object ID */
    public const val object_id: UInt = 0x2AC3_u

    /** Object Properties */
    public const val object_properties: UInt = 0x2AC4_u

    /** Object Action Control Point */
    public const val object_action_control_point: UInt = 0x2AC5_u

    /** Object List Control Point */
    public const val object_list_control_point: UInt = 0x2AC6_u

    /** Object List Filter */
    public const val object_list_filter: UInt = 0x2AC7_u

    /** Object Changed */
    public const val object_changed: UInt = 0x2AC8_u

    /** Resolvable Private Address Only */
    public const val resolvable_private_address_only: UInt = 0x2AC9_u

    /** Fitness Machine Feature */
    public const val fitness_machine_feature: UInt = 0x2ACC_u

    /** Treadmill Data */
    public const val treadmill_data: UInt = 0x2ACD_u

    /** Cross Trainer Data */
    public const val cross_trainer_data: UInt = 0x2ACE_u

    /** Step Climber Data */
    public const val step_climber_data: UInt = 0x2ACF_u

    /** Stair Climber Data */
    public const val stair_climber_data: UInt = 0x2AD0_u

    /** Rower Data */
    public const val rower_data: UInt = 0x2AD1_u

    /** Indoor Bike Data */
    public const val indoor_bike_data: UInt = 0x2AD2_u

    /** Training Status */
    public const val training_status: UInt = 0x2AD3_u

    /** Supported Speed Range */
    public const val supported_speed_range: UInt = 0x2AD4_u

    /** Supported Inclination Range */
    public const val supported_inclination_range: UInt = 0x2AD5_u

    /** Supported Resistance Level Range */
    public const val supported_resistance_level_range: UInt = 0x2AD6_u

    /** Supported Heart Rate Range */
    public const val supported_heart_rate_range: UInt = 0x2AD7_u

    /** Supported Power Range */
    public const val supported_power_range: UInt = 0x2AD8_u

    /** Fitness Machine Control Point */
    public const val fitness_machine_control_point: UInt = 0x2AD9_u

    /** Fitness Machine Status */
    public const val fitness_machine_status: UInt = 0x2ADA_u

    /** Mesh Provisioning Data In */
    public const val mesh_provisioning_data_in: UInt = 0x2ADB_u

    /** Mesh Provisioning Data Out */
    public const val mesh_provisioning_data_out: UInt = 0x2ADC_u

    /** Mesh Proxy Data In */
    public const val mesh_proxy_data_in: UInt = 0x2ADD_u

    /** Mesh Proxy Data Out */
    public const val mesh_proxy_data_out: UInt = 0x2ADE_u

    /** Average Current */
    public const val average_current: UInt = 0x2AE0_u

    /** Average Voltage */
    public const val average_voltage: UInt = 0x2AE1_u

    /** Boolean */
    public const val boolean: UInt = 0x2AE2_u

    /** Chromatic Distance from Planckian */
    public const val chromatic_distance_from_planckian: UInt = 0x2AE3_u

    /** Chromaticity Coordinates */
    public const val chromaticity_coordinates: UInt = 0x2AE4_u

    /** Chromaticity in CCT and Duv Values */
    public const val chromaticity_in_cct_and_duv_values: UInt = 0x2AE5_u

    /** Chromaticity Tolerance */
    public const val chromaticity_tolerance: UInt = 0x2AE6_u

    /** CIE 13.3-1995 Color Rendering Index */
    public const val cie_13_3_1995_color_rendering_index: UInt = 0x2AE7_u

    /** Coefficient */
    public const val coefficient: UInt = 0x2AE8_u

    /** Correlated Color Temperature */
    public const val correlated_color_temperature: UInt = 0x2AE9_u

    /** Count 16 */
    public const val count_16: UInt = 0x2AEA_u

    /** Count 24 */
    public const val count_24: UInt = 0x2AEB_u

    /** Country Code */
    public const val country_code: UInt = 0x2AEC_u

    /** Date UTC */
    public const val date_utc: UInt = 0x2AED_u

    /** Electric Current */
    public const val electric_current: UInt = 0x2AEE_u

    /** Electric Current Range */
    public const val electric_current_range: UInt = 0x2AEF_u

    /** Electric Current Specification */
    public const val electric_current_specification: UInt = 0x2AF0_u

    /** Electric Current Statistics */
    public const val electric_current_statistics: UInt = 0x2AF1_u

    /** Energy */
    public const val energy: UInt = 0x2AF2_u

    /** Energy in a Period of Day */
    public const val energy_in_a_period_of_day: UInt = 0x2AF3_u

    /** Event Statistics */
    public const val event_statistics: UInt = 0x2AF4_u

    /** Fixed String 16 */
    public const val fixed_string_16: UInt = 0x2AF5_u

    /** Fixed String 24 */
    public const val fixed_string_24: UInt = 0x2AF6_u

    /** Fixed String 36 */
    public const val fixed_string_36: UInt = 0x2AF7_u

    /** Fixed String 8 */
    public const val fixed_string_8: UInt = 0x2AF8_u

    /** Generic Level */
    public const val generic_level: UInt = 0x2AF9_u

    /** Global Trade Item Number */
    public const val global_trade_item_number: UInt = 0x2AFA_u

    /** Illuminance */
    public const val illuminance: UInt = 0x2AFB_u

    /** Luminous Efficacy */
    public const val luminous_efficacy: UInt = 0x2AFC_u

    /** Luminous Energy */
    public const val luminous_energy: UInt = 0x2AFD_u

    /** Luminous Exposure */
    public const val luminous_exposure: UInt = 0x2AFE_u

    /** Luminous Flux */
    public const val luminous_flux: UInt = 0x2AFF_u

    /** Luminous Flux Range */
    public const val luminous_flux_range: UInt = 0x2B00_u

    /** Luminous Intensity */
    public const val luminous_intensity: UInt = 0x2B01_u

    /** Mass Flow */
    public const val mass_flow: UInt = 0x2B02_u

    /** Perceived Lightness */
    public const val perceived_lightness: UInt = 0x2B03_u

    /** Percentage 8 */
    public const val percentage_8: UInt = 0x2B04_u

    /** Power */
    public const val power: UInt = 0x2B05_u

    /** Power Specification */
    public const val power_specification: UInt = 0x2B06_u

    /** Relative Runtime in a Current Range */
    public const val relative_runtime_in_a_current_range: UInt = 0x2B07_u

    /** Relative Runtime in a Generic Level Range */
    public const val relative_runtime_in_a_generic_level_range: UInt = 0x2B08_u

    /** Relative Value in a Voltage Range */
    public const val relative_value_in_a_voltage_range: UInt = 0x2B09_u

    /** Relative Value in an Illuminance Range */
    public const val relative_value_in_an_illuminance_range: UInt = 0x2B0A_u

    /** Relative Value in a Period of Day */
    public const val relative_value_in_a_period_of_day: UInt = 0x2B0B_u

    /** Relative Value in a Temperature Range */
    public const val relative_value_in_a_temperature_range: UInt = 0x2B0C_u

    /** Temperature 8 */
    public const val temperature_8: UInt = 0x2B0D_u

    /** Temperature 8 in a Period of Day */
    public const val temperature_8_in_a_period_of_day: UInt = 0x2B0E_u

    /** Temperature 8 Statistics */
    public const val temperature_8_statistics: UInt = 0x2B0F_u

    /** Temperature Range */
    public const val temperature_range: UInt = 0x2B10_u

    /** Temperature Statistics */
    public const val temperature_statistics: UInt = 0x2B11_u

    /** Time Decihour 8 */
    public const val time_decihour_8: UInt = 0x2B12_u

    /** Time Exponential 8 */
    public const val time_exponential_8: UInt = 0x2B13_u

    /** Time Hour 24 */
    public const val time_hour_24: UInt = 0x2B14_u

    /** Time Millisecond 24 */
    public const val time_millisecond_24: UInt = 0x2B15_u

    /** Time Second 16 */
    public const val time_second_16: UInt = 0x2B16_u

    /** Time Second 8 */
    public const val time_second_8: UInt = 0x2B17_u

    /** Voltage */
    public const val voltage: UInt = 0x2B18_u

    /** Voltage Specification */
    public const val voltage_specification: UInt = 0x2B19_u

    /** Voltage Statistics */
    public const val voltage_statistics: UInt = 0x2B1A_u

    /** Volume Flow */
    public const val volume_flow: UInt = 0x2B1B_u

    /** Chromaticity Coordinate */
    public const val chromaticity_coordinate: UInt = 0x2B1C_u

    /** RC Feature */
    public const val rc_feature: UInt = 0x2B1D_u

    /** RC Settings */
    public const val rc_settings: UInt = 0x2B1E_u

    /** Reconnection Configuration Control Point */
    public const val reconnection_configuration_control_point: UInt = 0x2B1F_u

    /** IDD Status Changed */
    public const val idd_status_changed: UInt = 0x2B20_u

    /** IDD Status */
    public const val idd_status: UInt = 0x2B21_u

    /** IDD Annunciation Status */
    public const val idd_annunciation_status: UInt = 0x2B22_u

    /** IDD Features */
    public const val idd_features: UInt = 0x2B23_u

    /** IDD Status Reader Control Point */
    public const val idd_status_reader_control_point: UInt = 0x2B24_u

    /** IDD Command Control Point */
    public const val idd_command_control_point: UInt = 0x2B25_u

    /** IDD Command Data */
    public const val idd_command_data: UInt = 0x2B26_u

    /** IDD Record Access Control Point */
    public const val idd_record_access_control_point: UInt = 0x2B27_u

    /** IDD History Data */
    public const val idd_history_data: UInt = 0x2B28_u

    /** Client Supported Features */
    public const val client_supported_features: UInt = 0x2B29_u

    /** Database Hash */
    public const val database_hash: UInt = 0x2B2A_u

    /** BSS Control Point */
    public const val bss_control_point: UInt = 0x2B2B_u

    /** BSS Response */
    public const val bss_response: UInt = 0x2B2C_u

    /** Emergency ID */
    public const val emergency_id: UInt = 0x2B2D_u

    /** Emergency Text */
    public const val emergency_text: UInt = 0x2B2E_u

    /** ACS Status */
    public const val acs_status: UInt = 0x2B2F_u

    /** ACS Data In */
    public const val acs_data_in: UInt = 0x2B30_u

    /** ACS Data Out Notify */
    public const val acs_data_out_notify: UInt = 0x2B31_u

    /** ACS Data Out Indicate */
    public const val acs_data_out_indicate: UInt = 0x2B32_u

    /** ACS Control Point */
    public const val acs_control_point: UInt = 0x2B33_u

    /** Enhanced Blood Pressure Measurement */
    public const val enhanced_blood_pressure_measurement: UInt = 0x2B34_u

    /** Enhanced Intermediate Cuff Pressure */
    public const val enhanced_intermediate_cuff_pressure: UInt = 0x2B35_u

    /** Blood Pressure Record */
    public const val blood_pressure_record: UInt = 0x2B36_u

    /** Registered User */
    public const val registered_user: UInt = 0x2B37_u

    /** BR-EDR Handover Data */
    public const val br_edr_handover_data: UInt = 0x2B38_u

    /** Bluetooth SIG Data */
    public const val bluetooth_sig_data: UInt = 0x2B39_u

    /** Server Supported Features */
    public const val server_supported_features: UInt = 0x2B3A_u

    /** Physical Activity Monitor Features */
    public const val physical_activity_monitor_features: UInt = 0x2B3B_u

    /** General Activity Instantaneous Data */
    public const val general_activity_instantaneous_data: UInt = 0x2B3C_u

    /** General Activity Summary Data */
    public const val general_activity_summary_data: UInt = 0x2B3D_u

    /** CardioRespiratory Activity Instantaneous Data */
    public const val cardiorespiratory_activity_instantaneous_data: UInt = 0x2B3E_u

    /** CardioRespiratory Activity Summary Data */
    public const val cardiorespiratory_activity_summary_data: UInt = 0x2B3F_u

    /** Step Counter Activity Summary Data */
    public const val step_counter_activity_summary_data: UInt = 0x2B40_u

    /** Sleep Activity Instantaneous Data */
    public const val sleep_activity_instantaneous_data: UInt = 0x2B41_u

    /** Sleep Activity Summary Data */
    public const val sleep_activity_summary_data: UInt = 0x2B42_u

    /** Physical Activity Monitor Control Point */
    public const val physical_activity_monitor_control_point: UInt = 0x2B43_u

    /** Physical Activity Current Session */
    public const val physical_activity_current_session: UInt = 0x2B44_u

    /** Physical Activity Session Descriptor */
    public const val physical_activity_session_descriptor: UInt = 0x2B45_u

    /** Preferred Units */
    public const val preferred_units: UInt = 0x2B46_u

    /** High Resolution Height */
    public const val high_resolution_height: UInt = 0x2B47_u

    /** Middle Name */
    public const val middle_name: UInt = 0x2B48_u

    /** Stride Length */
    public const val stride_length: UInt = 0x2B49_u

    /** Handedness */
    public const val handedness: UInt = 0x2B4A_u

    /** Device Wearing Position */
    public const val device_wearing_position: UInt = 0x2B4B_u

    /** Four Zone Heart Rate Limits */
    public const val four_zone_heart_rate_limits: UInt = 0x2B4C_u

    /** High Intensity Exercise Threshold */
    public const val high_intensity_exercise_threshold: UInt = 0x2B4D_u

    /** Activity Goal */
    public const val activity_goal: UInt = 0x2B4E_u

    /** Sedentary Interval Notification */
    public const val sedentary_interval_notification: UInt = 0x2B4F_u

    /** Caloric Intake */
    public const val caloric_intake: UInt = 0x2B50_u

    /** TMAP Role */
    public const val tmap_role: UInt = 0x2B51_u

    /** Audio Input State */
    public const val audio_input_state: UInt = 0x2B77_u

    /** Gain Settings Attribute */
    public const val gain_settings_attribute: UInt = 0x2B78_u

    /** Audio Input Type */
    public const val audio_input_type: UInt = 0x2B79_u

    /** Audio Input Status */
    public const val audio_input_status: UInt = 0x2B7A_u

    /** Audio Input Control Point */
    public const val audio_input_control_point: UInt = 0x2B7B_u

    /** Audio Input Description */
    public const val audio_input_description: UInt = 0x2B7C_u

    /** Volume State */
    public const val volume_state: UInt = 0x2B7D_u

    /** Volume Control Point */
    public const val volume_control_point: UInt = 0x2B7E_u

    /** Volume Flags */
    public const val volume_flags: UInt = 0x2B7F_u

    /** Volume Offset State */
    public const val volume_offset_state: UInt = 0x2B80_u

    /** Audio Location */
    public const val audio_location: UInt = 0x2B81_u

    /** Volume Offset Control Point */
    public const val volume_offset_control_point: UInt = 0x2B82_u

    /** Audio Output Description */
    public const val audio_output_description: UInt = 0x2B83_u

    /** Set Identity Resolving Key */
    public const val set_identity_resolving_key: UInt = 0x2B84_u

    /** Coordinated Set Size */
    public const val size_characteristic: UInt = 0x2B85_u

    /** Set Member Lock */
    public const val lock_characteristic: UInt = 0x2B86_u

    /** Set Member Rank */
    public const val rank_characteristic: UInt = 0x2B87_u

    /** Encrypted Data Key Material */
    public const val encrypted_data_key_material: UInt = 0x2B88_u

    /** Apparent Energy 32 */
    public const val apparent_energy_32: UInt = 0x2B89_u

    /** Apparent Power */
    public const val apparent_power: UInt = 0x2B8A_u

    /** Live Health Observations */
    public const val live_health_observations: UInt = 0x2B8B_u

    /** CO\textsubscript{2} Concentration */
    public const val co2_concentration: UInt = 0x2B8C_u

    /** Cosine of the Angle */
    public const val cosine_of_the_angle: UInt = 0x2B8D_u

    /** Device Time Feature */
    public const val device_time_feature: UInt = 0x2B8E_u

    /** Device Time Parameters */
    public const val device_time_parameters: UInt = 0x2B8F_u

    /** Device Time */
    public const val device_time: UInt = 0x2B90_u

    /** Device Time Control Point */
    public const val device_time_control_point: UInt = 0x2B91_u

    /** Time Change Log Data */
    public const val time_change_log_data: UInt = 0x2B92_u

    /** Media Player Name */
    public const val media_player_name: UInt = 0x2B93_u

    /** Media Player Icon Object ID */
    public const val media_player_icon_object_id: UInt = 0x2B94_u

    /** Media Player Icon URL */
    public const val media_player_icon_url: UInt = 0x2B95_u

    /** Track Changed */
    public const val track_changed: UInt = 0x2B96_u

    /** Track Title */
    public const val track_title: UInt = 0x2B97_u

    /** Track Duration */
    public const val track_duration: UInt = 0x2B98_u

    /** Track Position */
    public const val track_position: UInt = 0x2B99_u

    /** Playback Speed */
    public const val playback_speed: UInt = 0x2B9A_u

    /** Seeking Speed */
    public const val seeking_speed: UInt = 0x2B9B_u

    /** Current Track Segments Object ID */
    public const val current_track_segments_object_id: UInt = 0x2B9C_u

    /** Current Track Object ID */
    public const val current_track_object_id: UInt = 0x2B9D_u

    /** Next Track Object ID */
    public const val next_track_object_id: UInt = 0x2B9E_u

    /** Parent Group Object ID */
    public const val parent_group_object_id: UInt = 0x2B9F_u

    /** Current Group Object ID */
    public const val current_group_object_id: UInt = 0x2BA0_u

    /** Playing Order */
    public const val playing_order: UInt = 0x2BA1_u

    /** Playing Orders Supported */
    public const val playing_orders_supported: UInt = 0x2BA2_u

    /** Media State */
    public const val media_state: UInt = 0x2BA3_u

    /** Media Control Point */
    public const val media_control_point: UInt = 0x2BA4_u

    /** Media Control Point Opcodes Supported */
    public const val media_control_point_opcodes_supported: UInt = 0x2BA5_u

    /** Search Results Object ID */
    public const val search_results_object_id: UInt = 0x2BA6_u

    /** Search Control Point */
    public const val search_control_point: UInt = 0x2BA7_u

    /** Energy 32 */
    public const val energy_32: UInt = 0x2BA8_u

    /** Constant Tone Extension Enable */
    public const val constant_tone_extension_enable: UInt = 0x2BAD_u

    /** Advertising Constant Tone Extension Minimum Length */
    public const val advertising_constant_tone_extension_minimum_length: UInt = 0x2BAE_u

    /** Advertising Constant Tone Extension Minimum Transmit Count */
    public const val advertising_constant_tone_extension_minimum_transmit_count: UInt = 0x2BAF_u

    /** Advertising Constant Tone Extension Transmit Duration */
    public const val advertising_constant_tone_extension_transmit_duration: UInt = 0x2BB0_u

    /** Advertising Constant Tone Extension Interval */
    public const val advertising_constant_tone_extension_interval: UInt = 0x2BB1_u

    /** Advertising Constant Tone Extension PHY */
    public const val advertising_constant_tone_extension_phy: UInt = 0x2BB2_u

    /** Bearer Provider Name */
    public const val bearer_provider_name: UInt = 0x2BB3_u

    /** Bearer UCI */
    public const val bearer_uci: UInt = 0x2BB4_u

    /** Bearer Technology */
    public const val bearer_technology: UInt = 0x2BB5_u

    /** Bearer URI Schemes Supported List */
    public const val bearer_uri_schemes_supported_list: UInt = 0x2BB6_u

    /** Bearer Signal Strength */
    public const val bearer_signal_strength: UInt = 0x2BB7_u

    /** Bearer Signal Strength Reporting Interval */
    public const val bearer_signal_strength_reporting_interval: UInt = 0x2BB8_u

    /** Bearer List Current Calls */
    public const val bearer_list_current_calls: UInt = 0x2BB9_u

    /** Content Control ID */
    public const val content_control_id: UInt = 0x2BBA_u

    /** Status Flags */
    public const val status_flags: UInt = 0x2BBB_u

    /** Incoming Call Target Bearer URI */
    public const val incoming_call_target_bearer_uri: UInt = 0x2BBC_u

    /** Call State */
    public const val call_state: UInt = 0x2BBD_u

    /** Call Control Point */
    public const val call_control_point: UInt = 0x2BBE_u

    /** Call Control Point Optional Opcodes */
    public const val call_control_point_optional_opcodes: UInt = 0x2BBF_u

    /** Termination Reason */
    public const val termination_reason: UInt = 0x2BC0_u

    /** Incoming Call */
    public const val incoming_call: UInt = 0x2BC1_u

    /** Call Friendly Name */
    public const val call_friendly_name: UInt = 0x2BC2_u

    /** Mute */
    public const val mute: UInt = 0x2BC3_u

    /** Sink ASE */
    public const val sink_ase: UInt = 0x2BC4_u

    /** Source ASE */
    public const val source_ase: UInt = 0x2BC5_u

    /** ASE Control Point */
    public const val ase_control_point: UInt = 0x2BC6_u

    /** Broadcast Audio Scan Control Point */
    public const val broadcast_audio_scan_control_point: UInt = 0x2BC7_u

    /** Broadcast Receive State */
    public const val broadcast_receive_state: UInt = 0x2BC8_u

    /** Sink PAC */
    public const val sink_pac: UInt = 0x2BC9_u

    /** Sink Audio Locations */
    public const val sink_audio_locations: UInt = 0x2BCA_u

    /** Source PAC */
    public const val source_pac: UInt = 0x2BCB_u

    /** Source Audio Locations */
    public const val source_audio_locations: UInt = 0x2BCC_u

    /** Available Audio Contexts */
    public const val available_audio_contexts: UInt = 0x2BCD_u

    /** Supported Audio Contexts */
    public const val supported_audio_contexts: UInt = 0x2BCE_u

    /** Ammonia Concentration */
    public const val ammonia_concentration: UInt = 0x2BCF_u

    /** Carbon Monoxide Concentration */
    public const val carbon_monoxide_concentration: UInt = 0x2BD0_u

    /** Methane Concentration */
    public const val methane_concentration: UInt = 0x2BD1_u

    /** Nitrogen Dioxide Concentration */
    public const val nitrogen_dioxide_concentration: UInt = 0x2BD2_u

    /** Non-Methane Volatile Organic Compounds Concentration */
    public const val `non-methane_volatile_organic_compounds_concentration`: UInt = 0x2BD3_u

    /** Ozone Concentration */
    public const val ozone_concentration: UInt = 0x2BD4_u

    /** Particulate Matter - PM1 Concentration */
    public const val particulate_matter_pm1_concentration: UInt = 0x2BD5_u

    /** Particulate Matter - PM2.5 Concentration */
    public const val particulate_matter_pm2_5_concentration: UInt = 0x2BD6_u

    /** Particulate Matter - PM10 Concentration */
    public const val particulate_matter_pm10_concentration: UInt = 0x2BD7_u

    /** Sulfur Dioxide Concentration */
    public const val sulfur_dioxide_concentration: UInt = 0x2BD8_u

    /** Sulfur Hexafluoride Concentration */
    public const val sulfur_hexafluoride_concentration: UInt = 0x2BD9_u

    /** Hearing Aid Features */
    public const val hearing_aid_features: UInt = 0x2BDA_u

    /** Hearing Aid Preset Control Point */
    public const val hearing_aid_preset_control_point: UInt = 0x2BDB_u

    /** Active Preset Index */
    public const val active_preset_index: UInt = 0x2BDC_u

    /** Stored Health Observations */
    public const val stored_health_observations: UInt = 0x2BDD_u

    /** Fixed String 64 */
    public const val fixed_string_64: UInt = 0x2BDE_u

    /** High Temperature */
    public const val high_temperature: UInt = 0x2BDF_u

    /** High Voltage */
    public const val high_voltage: UInt = 0x2BE0_u

    /** Light Distribution */
    public const val light_distribution: UInt = 0x2BE1_u

    /** Light Output */
    public const val light_output: UInt = 0x2BE2_u

    /** Light Source Type */
    public const val light_source_type: UInt = 0x2BE3_u

    /** Noise */
    public const val noise: UInt = 0x2BE4_u

    /** Relative Runtime in a Correlated Color Temperature Range */
    public const val relative_runtime_in_a_correlated_color_temperature_range: UInt = 0x2BE5_u

    /** Time Second 32 */
    public const val time_second_32: UInt = 0x2BE6_u

    /** VOC Concentration */
    public const val voc_concentration: UInt = 0x2BE7_u

    /** Voltage Frequency */
    public const val voltage_frequency: UInt = 0x2BE8_u

    /** Battery Critical Status */
    public const val battery_critical_status: UInt = 0x2BE9_u

    /** Battery Health Status */
    public const val battery_health_status: UInt = 0x2BEA_u

    /** Battery Health Information */
    public const val battery_health_information: UInt = 0x2BEB_u

    /** Battery Information */
    public const val battery_information: UInt = 0x2BEC_u

    /** Battery Level Status */
    public const val battery_level_status: UInt = 0x2BED_u

    /** Battery Time Status */
    public const val battery_time_status: UInt = 0x2BEE_u

    /** Estimated Service Date */
    public const val estimated_service_date: UInt = 0x2BEF_u

    /** Battery Energy Status */
    public const val battery_energy_status: UInt = 0x2BF0_u

    /** Observation Schedule Changed */
    public const val observation_schedule_changed: UInt = 0x2BF1_u

    /** Current Elapsed Time */
    public const val current_elapsed_time: UInt = 0x2BF2_u

    /** Health Sensor Features */
    public const val health_sensor_features: UInt = 0x2BF3_u

    /** GHS Control Point */
    public const val ghs_control_point: UInt = 0x2BF4_u

    /** LE GATT Security Levels */
    public const val le_gatt_security_levels: UInt = 0x2BF5_u

    /** ESL Address */
    public const val esl_address: UInt = 0x2BF6_u

    /** AP Sync Key Material */
    public const val ap_sync_key_material: UInt = 0x2BF7_u

    /** ESL Response Key Material */
    public const val esl_response_key_material: UInt = 0x2BF8_u

    /** ESL Current Absolute Time */
    public const val esl_current_absolute_time: UInt = 0x2BF9_u

    /** ESL Display Information */
    public const val esl_display_information: UInt = 0x2BFA_u

    /** ESL Image Information */
    public const val esl_image_information: UInt = 0x2BFB_u

    /** ESL Sensor Information */
    public const val esl_sensor_information: UInt = 0x2BFC_u

    /** ESL LED Information */
    public const val esl_led_information: UInt = 0x2BFD_u

    /** ESL Control Point */
    public const val esl_control_point: UInt = 0x2BFE_u

    /** UDI for Medical Devices */
    public const val medical_devices: UInt = 0x2BFF_u

    /** GMAP Role */
    public const val gmap_role: UInt = 0x2C00_u

    /** UGG Features */
    public const val ugg_features: UInt = 0x2C01_u

    /** UGT Features */
    public const val ugt_features: UInt = 0x2C02_u

    /** BGS Features */
    public const val bgs_features: UInt = 0x2C03_u

    /** BGR Features */
    public const val bgr_features: UInt = 0x2C04_u

    /** Percentage 8 Steps */
    public const val percentage_8_steps: UInt = 0x2C05_u

    /** Acceleration */
    public const val acceleration: UInt = 0x2C06_u

    /** Force */
    public const val force: UInt = 0x2C07_u

    /** Linear Position */
    public const val linear_position: UInt = 0x2C08_u

    /** Rotational Speed */
    public const val rotational_speed: UInt = 0x2C09_u

    /** Length */
    public const val length: UInt = 0x2C0A_u

    /** Torque */
    public const val torque: UInt = 0x2C0B_u

    /** IMD Status */
    public const val imd_status: UInt = 0x2C0C_u

    /** IMDS Descriptor Value Changed */
    public const val imds_descriptor_value_changed: UInt = 0x2C0D_u

    /** First Use Date */
    public const val first_use_date: UInt = 0x2C0E_u

    /** Life Cycle Data */
    public const val life_cycle_data: UInt = 0x2C0F_u

    /** Work Cycle Data */
    public const val work_cycle_data: UInt = 0x2C10_u

    /** Service Cycle Data */
    public const val service_cycle_data: UInt = 0x2C11_u

    /** IMD Control */
    public const val imd_control: UInt = 0x2C12_u

    /** IMD Historical Data */
    public const val imd_historical_data: UInt = 0x2C13_u

    /** RAS Features */
    public const val ras_features: UInt = 0x2C14_u

    /** Real-time Ranging Data */
    public const val `real-time_ranging_data`: UInt = 0x2C15_u

    /** On-demand Ranging Data */
    public const val `on-demand_ranging_data`: UInt = 0x2C16_u

    /** RAS Control Point */
    public const val ras_control_point: UInt = 0x2C17_u

    /** Ranging Data Ready */
    public const val ranging_data_ready: UInt = 0x2C18_u

    /** Ranging Data Overwritten */
    public const val ranging_data_overwritten: UInt = 0x2C19_u
}

/**
 * Bluetooth SIG assigned numbers for descriptors.
 *
 * Constant names exactly match the IDs defined in the Bluetooth SIG assigned numbers YAML file
 * (e.g. `org.bluetooth.descriptor.gatt.client_characteristic_configuration`).
 *
 * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/uuids/descriptor_uuids.yaml
 */
public object descriptor {
    public object gatt {
        /** Characteristic Extended Properties */
        public const val characteristic_extended_properties: UInt = 0x2900_u

        /** Characteristic User Description */
        public const val characteristic_user_description: UInt = 0x2901_u

        /** Client Characteristic Configuration */
        public const val client_characteristic_configuration: UInt = 0x2902_u

        /** Server Characteristic Configuration */
        public const val server_characteristic_configuration: UInt = 0x2903_u

        /** Characteristic Presentation Format */
        public const val characteristic_presentation_format: UInt = 0x2904_u

        /** Characteristic Aggregate Format */
        public const val characteristic_aggregate_format: UInt = 0x2905_u
    }

    /** Valid Range */
    public const val valid_range: UInt = 0x2906_u

    /** External Report Reference */
    public const val external_report_reference: UInt = 0x2907_u

    /** Report Reference */
    public const val report_reference: UInt = 0x2908_u

    /** Number of Digitals */
    public const val number_of_digitals: UInt = 0x2909_u

    /** Value Trigger Setting */
    public const val value_trigger_setting: UInt = 0x290A_u

    /** Environmental Sensing Configuration */
    public const val es_configuration: UInt = 0x290B_u

    /** Environmental Sensing Measurement */
    public const val es_measurement: UInt = 0x290C_u

    /** Environmental Sensing Trigger Setting */
    public const val es_trigger_setting: UInt = 0x290D_u

    /** Time Trigger Setting */
    public const val time_trigger_setting: UInt = 0x290E_u

    /** Complete BR-EDR Transport Block Data */
    public const val complete_br_edr_transport_block_data: UInt = 0x290F_u

    /** Observation Schedule */
    public const val observation_schedule: UInt = 0x2910_u

    /** Valid Range and Accuracy */
    public const val valid_range_accuracy: UInt = 0x2911_u

    /** Measurement Description */
    public const val measurement_description: UInt = 0x2912_u

    /** Manufacturer Limits */
    public const val manufacturer_limits: UInt = 0x2913_u

    /** Process Tolerances */
    public const val process_tolerances: UInt = 0x2914_u

    /** IMD Trigger Setting */
    public const val imd_trigger_setting: UInt = 0x2915_u
}
