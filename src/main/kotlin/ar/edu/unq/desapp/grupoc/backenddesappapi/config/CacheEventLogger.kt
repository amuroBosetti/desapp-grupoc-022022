package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener

class CacheEventLogger : CacheEventListener<Any, Any>{

    override fun onEvent(event: CacheEvent<out Any, out Any>?) {
        // TODO esto no se llama por algun motivo
        print("$event.key, $event.oldValue, $event.newValue")
    }

}