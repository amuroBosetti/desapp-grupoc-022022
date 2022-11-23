package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class CacheEventLogger : CacheEventListener<String, ResponseEntity<Any>>{

    override fun onEvent(event: CacheEvent<out String, out ResponseEntity<Any>>?) {
        // TODO esto no se llama por algun motivo
        print("$event.key, $event.oldValue, $event.newValue")
    }

}