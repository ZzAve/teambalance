package nl.jvandis.teambalance.api.event

import java.util.UUID

@JvmInline
value class RecurringEventPropertiesId(val value: String) {
    companion object {
        fun create(): RecurringEventPropertiesId = RecurringEventPropertiesId(UUID.randomUUID().toString())
    }
}
