package nl.jvandis.teambalance

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.loggerFor(): Logger {
    println("Creating a logger for ${T::class.java}")
    return LoggerFactory.getLogger(T::class.java)
}
