package com.juul.kable

import com.benasher44.uuid.Uuid

public actual sealed class Filter {

    public actual class Service actual constructor(
        public actual val uuid: Uuid,
    ) : Filter()
}
