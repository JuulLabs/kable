package com.juul.sensortag.permissions

public actual open class DeniedException : Exception()
public actual class DeniedAlwaysException : DeniedException()
