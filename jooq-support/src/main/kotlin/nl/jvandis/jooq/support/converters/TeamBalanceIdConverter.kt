package nl.jvandis.jooq.support.converters

import org.jooq.Converter

/**
 * JOOQ converter for Kotlin value class TeamBalanceId.
 *
 * This converter handles the JOOQ bug where value classes are incorrectly serialized.
 * See: https://github.com/jOOQ/jOOQ/issues/14533
 *
 * Without this converter, JOOQ would serialize TeamBalanceId as {"value": true} instead
 * of the actual string value.
 */
class TeamBalanceIdConverter : Converter<String, Any> {
    override fun from(databaseObject: String?): Any? = if (databaseObject == null) null else databaseObject

    override fun to(userObject: Any?): String? =
        when (userObject) {
            null -> null
            is String -> userObject
            else -> {
                // Handle the case where it's a TeamBalanceId instance and extract the value
                try {
                    val valueField = userObject.javaClass.getDeclaredField("value")
                    valueField.isAccessible = true
                    valueField.get(userObject) as String
                } catch (e: Exception) {
                    // Fallback to toString if reflection fails
                    userObject.toString()
                }
            }
        }

    override fun fromType(): Class<String> = String::class.java

    override fun toType(): Class<Any> = Any::class.java
}
