package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}
