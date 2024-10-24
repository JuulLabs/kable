package com.juul.sensortag.permissions

public expect open class DeniedException : Exception
public expect class DeniedAlwaysException : DeniedException
