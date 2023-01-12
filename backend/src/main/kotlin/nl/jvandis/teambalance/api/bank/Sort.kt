package nl.jvandis.teambalance.api.bank

import org.springframework.core.convert.converter.Converter

enum class Sort {
    DESC,
    ASC;

    companion object {
        fun fromValue(value: String): Sort {
            return values()
                .firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Unknown sort type " + value + ", Allowed values are " + values().contentToString()
                )
        }
    }
}

class StringToEnumConverter : Converter<String, Sort> {
    override fun convert(source: String): Sort {
        return Sort.fromValue(source)
    }
}
