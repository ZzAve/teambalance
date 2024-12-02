package nl.jvandis.teambalance.api

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import nl.jvandis.teambalance.Tenant
import java.time.Duration

/**
 * Sets up an asynchronous loading cache using the given configuration.
 *
 * @param config the cache configuration details.
 * @param loadingFunction the function to load cache values.
 * @return an instance of [AsyncLoadingCache] configured based on the supplied [CacheConfig].
 */
fun <K, V> setupCache(
    config: CacheConfig,
    loadingFunction: (K) -> V,
): AsyncLoadingCache<K, V> =
    Caffeine.newBuilder()
        .expireAfterWrite(config.expireAfterWrite)
        .apply { if (config.refreshAfterWrite != null) refreshAfterWrite(config.refreshAfterWrite) }
        .maximumSize(
            if (config.enabled) {
                config.maxSize ?: Tenant.entries.size.toLong()
            } else {
                0
            },
        )
        .buildAsync { key -> loadingFunction(key) }

/**
 * Configuration for setting up a cache.
 *
 * @property enabled indicates if the cache is enabled.
 * @property maxSize the maximum size of the cache, if applicable.
 * @property expireAfterWrite the duration after which the cache entries expire.
 * @property refreshAfterWrite the optional duration after which cache entries are refreshed.
 */
data class CacheConfig(
    val enabled: Boolean = true,
    val maxSize: Long?,
    val expireAfterWrite: Duration,
    val refreshAfterWrite: Duration?,
)
