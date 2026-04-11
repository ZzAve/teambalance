package nl.jvandis.teambalance

import java.util.UUID

@JvmInline
value class TeamBalanceId private constructor(
    val value: String,
) {
    companion object {
        fun create() = invoke(UUID.randomUUID().toString())

        fun random() = create()

        @JvmName("of")
        operator fun invoke(value: String) = TeamBalanceId(value)
    }
}
