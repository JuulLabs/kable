package com.juul.kable

/** Mode specifying if config descriptor (0x2902) should be written to when starting/stopping an observation. */
@Deprecated(
    message = "Writing notification descriptor is handled automatically by 'observe' function. This class is no longer used and will be removed in a future release.",
    level = DeprecationLevel.ERROR,
)
public enum class WriteNotificationDescriptor {

    /**
     * Always write to config descriptor for characteristic being observed. If it does not exist then an exception is
     * thrown when starting/stopping the observation.
     *
     * This is the default configuration on Android and matches the only supported behavior for Apple and JavaScript.
     */
    Always,

    /**
     * Never write to config descriptor for characteristic being observed, regardless of its availability.
     *
     * **Warning:** This option is only supported on Android. If the remote peripheral does not have a config descriptor
     * associated with characteristic being observed, then the observation will only work on Android and will fail on
     * other targets (e.g. Apple, JavaScript).
     */
    Never,

    /**
     * If config descriptor exists for characteristic being observed, then it will be written to when starting/stopping
     * observations. If it does not exist, then automatically fallback to only enabling/disabling notifications (without
     * writing to config descriptor).
     *
     * **Warning:** This option is only supported on Android. If the remote peripheral does not have a config descriptor
     * associated with characteristic being observed, then the observation will only work on Android and will fail on
     * other targets (e.g. Apple, JavaScript).
     */
    Auto,
}
