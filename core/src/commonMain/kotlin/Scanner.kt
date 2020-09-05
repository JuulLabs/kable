package com.juul.kable

import kotlinx.coroutines.flow.Flow

public expect class Scanner {
    public val peripherals: Flow<Advertisement>
}
