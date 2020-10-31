package com.juul.kable

import kotlinx.coroutines.flow.Flow

public class AndroidScanner internal constructor() : Scanner {

    public override val peripherals: Flow<Advertisement>
        get() = TODO("Not yet implemented")
}
