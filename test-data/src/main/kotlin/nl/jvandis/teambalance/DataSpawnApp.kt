package nl.jvandis.teambalance

import ch.qos.logback.classic.Level
import nl.jvandis.teambalance.testdata.Initializer
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.time.measureTime
import ch.qos.logback.classic.Logger as LogbackLogger

fun main(args: Array<String>) {
    val rootLogger: LogbackLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as LogbackLogger
    rootLogger.level = (Level.INFO)

    val log: Logger = LoggerFactory.getLogger("DataSpawnApp")

    val apiKey = args[0]

    val config =
        SpawnDataConfig(
            amountOfTrainings = 5,
            amountOfMatches = 5,
            amountOfEvents = 0,
        )

    // Use a 'controlled' random instance. For reproducibility, use a static seed
    val seed = Random.nextLong()
    // val seed = 1L
    val random = Random(seed)
    println("Using random with $seed")

    log.info("Running SpawnData ...")
    val measureTime =
        measureTime {
            Initializer(apiKey, "http://localhost:8080", random).spawnData(config)
        }

    log.info("Finished running SpawnData of $config in $measureTime...")
}
