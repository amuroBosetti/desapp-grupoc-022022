package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers.HighPerformanceQuotationsController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class HighPerformanceQuotationsUpdateWorker {

    @Autowired
    lateinit var highPerformanceQuotationsController : HighPerformanceQuotationsController

    @CacheEvict(cacheNames = ["priceCache"], allEntries = true, beforeInvocation = true)
    @Scheduled(cron = "* 0/10 * * * *")
    fun refreshCache(){
        print("Refreshing prices cache")
        highPerformanceQuotationsController.getAllTokenPrices()
    }

}