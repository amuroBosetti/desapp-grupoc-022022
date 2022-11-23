package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.event.EventType
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity

@Configuration
@EnableCaching
class CacheConfig {

    init {
        val cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(CacheEventLogger(), EventType.CREATED, EventType.UPDATED)
            .unordered().asynchronous()

        CacheManagerBuilder.newCacheManagerBuilder().withCache(
            "priceCache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String::class.java,
                ResponseEntity::class.java,
                ResourcePoolsBuilder.heap(10)
            )
                .withService(cacheEventListenerConfiguration)
        ).build(true)
    }

}