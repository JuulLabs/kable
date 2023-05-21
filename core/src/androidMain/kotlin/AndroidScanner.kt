package com.juul.kable

import kotlinx.coroutines.flow.Flow

public interface AndroidScanner : Scanner {
    public override val advertisements: Flow<AndroidAdvertisement>
}
