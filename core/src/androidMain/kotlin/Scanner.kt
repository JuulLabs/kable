package com.juul.kable

import kotlinx.coroutines.flow.Flow

public actual class Scanner internal constructor() {

    public actual val peripherals: Flow<Advertisement>
        get() = TODO("Not yet implemented")
}
