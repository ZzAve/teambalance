package nl.jvandis.teambalance.api.event

import org.slf4j.Logger

internal interface LoggingContext {
    fun log(): Logger
}
