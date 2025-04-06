package nl.jvandis.teambalance

import ch.qos.logback.classic.Level
import nl.jvandis.teambalance.testdata.Initializer
import nl.jvandis.teambalance.testdata.SpawnDataConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.time.measureTime
import ch.qos.logback.classic.Logger as LogbackLogger

private const val DEFAULT_HOST = "http://localhost:8080"

fun main(args: Array<String>) {
    val rootLogger: LogbackLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as LogbackLogger
    rootLogger.level = (Level.INFO)

    val log: Logger = LoggerFactory.getLogger("DataSpawnApp")

    if (args.contains("--help")) {
        println(
            """
            Usage: DataSpawnApp [options]
            Options:
                --apiKey=<apiKey>       The API key for authentication.
                --randomSeed=<seed>     The seed for random number generation.
                --host=<host>           The host (default: http://localhost:8080).
                --help                  Show this help message.
            """.trimIndent(),
        )
        return
    }

    val apiKey =
        namedArg(args, "apiKey") {
            System.getenv("TEAMBALANCE_API_KEY")
                ?: "".also { log.warn("No API key provided, using empty string") }
        }
    val username =
        namedArg(args, "username") {
            System.getenv("TEAMBALANCE_USERNAME")
                ?: "".also { log.warn("No username provided, using empty string") }
        }
    val password =
        namedArg(args, "password") {
            System.getenv("TEAMBALANCE_PASSWORD")
                ?: "".also { log.warn("No password provided, using empty string") }
        }
    val seed = namedArg(args, "randomSeed") { Random.nextLong().toString() }.toLong()
    val host = namedArg(args, "host") { DEFAULT_HOST }

    val config =
        SpawnDataConfig(
            amountOfUsers = 5,
            amountOfTrainings = 5,
            amountOfMatches = 5,
            amountOfEvents = 2,
            amountOfAliases = 5,
            amountOfTransactionExclusions = 10,
        )

    val random = Random(seed)
    log.info("Running SpawnData with config: $config, apiKey: ${apiKey.take(3)}***, seed: $seed, host: $host ...")
    val time =
        measureTime {
            Initializer(
                apiKey = apiKey,
                username = username,
                password = password,
                host = host,
                random = random,
                config = config,
            ).spawnData()
        }

    log.info("Finished running SpawnData of $config in $time...")
}

private fun namedArg(
    args: Array<String>,
    prefix: String,
    defaultBlock: () -> String,
) = args.find { it.startsWith("--$prefix=") }?.substringAfter("=") ?: defaultBlock()
