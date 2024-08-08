package com.juul.kable

import com.benasher44.uuid.Uuid

@Deprecated(
    message = "Use Options builder instead. See https://github.com/JuulLabs/kable/issues/723 for details.",
    replaceWith = ReplaceWith("Options { }"),
    level = DeprecationLevel.ERROR,
)
public fun Options(
    filters: List<Filter>? = null,
    filterSets: List<FilterSet>? = null,
    optionalServices: List<Uuid>? = null,
): Options = throw NotImplementedError("Use Options builder instead. See https://github.com/JuulLabs/kable/issues/723 for details.")
