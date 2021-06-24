package com.juul.kable

import com.benasher44.uuid.Uuid
import kotlinx.coroutines.flow.Flow

public interface Scanner {
    public val advertisements: Flow<Advertisement>
}

public expect fun Scanner(services: List<Uuid>? = null): Scanner
