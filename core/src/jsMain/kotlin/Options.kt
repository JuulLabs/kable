package com.juul.kable

/** https://developer.mozilla.org/en-US/docs/Web/API/Bluetooth/requestDevice */
public data class Options(
    val optionalServices: Array<String> = emptyArray(),
    val filters: Array<Filter>? = null,
) {

    public sealed class Filter {

        public data class Name(val name: String) : Filter()

        public data class NamePrefix(val namePrefix: String) : Filter()

        public data class Services(val services: Array<String>) : Filter() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class.js != other::class.js) return false
                other as Services
                if (!services.contentEquals(other.services)) return false
                return true
            }

            override fun hashCode(): Int {
                return services.contentHashCode()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false
        other as Options
        if (!optionalServices.contentEquals(other.optionalServices)) return false
        if (filters != null) {
            if (other.filters == null) return false
            if (!filters.contentEquals(other.filters)) return false
        } else if (other.filters != null) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = optionalServices.contentHashCode()
        result = 31 * result + (filters?.contentHashCode() ?: 0)
        return result
    }
}
