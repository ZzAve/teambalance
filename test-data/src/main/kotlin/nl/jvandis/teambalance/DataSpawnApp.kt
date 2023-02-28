package nl.jvandis.teambalance

import ch.qos.logback.classic.Level
import nl.jvandis.teambalance.testdata.Initializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger


fun main(args: Array<String>) {
    val rootLogger: LogbackLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as LogbackLogger
    rootLogger.level = (Level.INFO)

    val log: Logger = LoggerFactory.getLogger("DataSpawnApp")

    val apiKey = args[0];

    log.info("Running SpawnData ...")
    Initializer(apiKey).spawnData()

    log.info("Finished running SpawnData ...")
}
