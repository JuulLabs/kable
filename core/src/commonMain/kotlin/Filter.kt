package com.juul.kable

import com.benasher44.uuid.Uuid

public expect sealed class Filter {

    public class Service constructor(uuid: Uuid) : Filter {
        public val uuid: Uuid
    }
}
