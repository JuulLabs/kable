package dev.icerock.moko.permissions

interface Permission {
    val delegate: PermissionDelegate

    // Extended by individual permission delegates
    companion object
}
