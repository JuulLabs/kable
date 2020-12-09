package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}

// todo: Add support for specifying services to filter against (for platforms that support it).
// https://github.com/JuulLabs/kable/issues/22
public expect fun Scanner(): Scanner
