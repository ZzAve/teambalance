package nl.jvandis.teambalance.api

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import nl.jvandis.teambalance.loggerFor
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ConfigurationService(
    private val repository: ConfigurationRepository,
    private val objectMapper: ObjectMapper
) {
    fun <T : Any> getConfig(key: String, clazz: KClass<T>): T {
        return try {
            repository.getConfig(key)
                ?.let { objectMapper.readValue(it, clazz.java) }
                ?: throw NoConfigFound("There was no configuration found for config with key '$key'")
        } catch (e: JsonProcessingException) {
            throw MalformedConfigFound("Config for '$key' seems to be malformed. Expected type $clazz", e)
        } catch (e: JsonMappingException) {
            throw MalformedConfigFound("Config for '$key' seems to be malformed. Expected type $clazz", e)
        }
    }

    fun <T : Any> getConfig(key: String, clazz: KClass<T>, default: T): T {
        return try {
            getConfig(key, clazz)
        } catch (e: NoConfigFound) {
            LOG.warn("Could not find any config for '$key'. Using fallback  $default")
            default
        }
    }

    companion object {
        private val LOG = loggerFor()
    }
}

sealed class ConfigurationServiceException(
    message: String,
    exception: Exception? = null
) : RuntimeException(message, exception)

class NoConfigFound(
    message: String,
    exception: Exception? = null
) : ConfigurationServiceException(message, exception)

class MalformedConfigFound(
    message: String,
    exception: Exception? = null
) : ConfigurationServiceException(message, exception)
