package nl.jvandis.teambalance.api

import nl.jvandis.teambalance.filters.DEFAULT_START_OF_SEASON_RAW
import nl.jvandis.teambalance.filters.toZonedDateTime
import nl.jvandis.teambalance.log
import org.springframework.stereotype.Service
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass

const val START_OF_SEASON_CONFIG_KEY = "startOfSeason"

@Service
class ConfigurationService(
    private val repository: ConfigurationRepository,
    private val objectMapper: ObjectMapper,
) {
    fun <T : Any> getConfig(
        key: String,
        clazz: KClass<T>,
    ): T =
        try {
            repository
                .getConfig(key)
                ?.let { objectMapper.readValue(it, clazz.java) }
                ?: throw NoConfigFound("There was no configuration found for config with key '$key'")
        } catch (e: JacksonException) {
            throw MalformedConfigFound("Config for '$key' seems to be malformed. Expected type $clazz", e)
        }

    fun <T : Any> getConfig(
        key: String,
        clazz: KClass<T>,
        default: T,
    ): T =
        try {
            getConfig(key, clazz)
        } catch (e: NoConfigFound) {
            log.warn("Could not find any config for '$key'. Using fallback  $default")
            default
        }

    fun getStartOfSeason(): LocalDateTime {
        val raw = repository.getConfig(START_OF_SEASON_CONFIG_KEY) ?: DEFAULT_START_OF_SEASON_RAW
        return try {
            LocalDateTime.parse(raw)
        } catch (e: DateTimeParseException) {
            throw MalformedConfigFound(
                "Config for '$START_OF_SEASON_CONFIG_KEY' seems to be malformed. Expected ISO local date time",
                e,
            )
        }
    }

    fun getStartOfSeasonZoned(): ZonedDateTime = getStartOfSeason().toZonedDateTime()

    fun setStartOfSeason(startOfSeason: LocalDateTime) {
        repository.upsertConfig(START_OF_SEASON_CONFIG_KEY, startOfSeason.toString())
    }
}

sealed class ConfigurationServiceException(
    message: String,
    exception: Exception? = null,
) : RuntimeException(message, exception)

class NoConfigFound(
    message: String,
    exception: Exception? = null,
) : ConfigurationServiceException(message, exception)

class MalformedConfigFound(
    message: String,
    exception: Exception? = null,
) : ConfigurationServiceException(message, exception)
