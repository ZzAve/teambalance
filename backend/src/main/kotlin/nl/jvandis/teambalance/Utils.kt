package nl.jvandis.teambalance

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
