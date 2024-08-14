package nl.jvandis.teambalance

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Prefer T.loggerFor()
 */
fun loggerFor(loggerName: String): Logger {
    println("Creating a logger for $loggerName")
    return LoggerFactory.getLogger(loggerName)
}

@Deprecated(message = "")
inline fun <reified T> T.loggerFor(): Logger {
    println("Creating a logger for ${T::class.java}")
    return LoggerFactory.getLogger(T::class.java)
}

internal inline val <reified T> T.log: Logger
    get() = LoggerFactory.getLogger(T::class.java)

@JvmInline
value class TeamBalanceId private constructor(val value: String) {
    companion object {
        fun create() = invoke(UUID.randomUUID().toString())

        fun random() = create()

        @JvmName("of")
        operator fun invoke(value: String) = TeamBalanceId(value)
    }
}
