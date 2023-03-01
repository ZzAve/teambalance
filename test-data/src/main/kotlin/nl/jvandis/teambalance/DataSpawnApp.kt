package nl.jvandis.teambalance

import ch.qos.logback.classic.Level
import nl.jvandis.teambalance.testdata.Initializer
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import ch.qos.logback.classic.Logger as LogbackLogger


@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val rootLogger: LogbackLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as LogbackLogger
    rootLogger.level = (Level.INFO)

    val log: Logger = LoggerFactory.getLogger("DataSpawnApp")

    val apiKey = args[0];

    val config = SpawnDataConfig(
        amountOfTrainings = 100,
        amountOfMatches = 100,
        amountOfEvents = 100
    )

    log.info("Running SpawnData ...")
    val measureTime = measureTime {
        Initializer(apiKey).spawnData(config)
    }

    log.info("Finished running SpawnData of $config in $measureTime...")
}
