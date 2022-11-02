package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.DomainType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class BinanceApiRestClientBeanConfiguration {

    @Bean(name = ["binanceApiRestClient"])
    fun getApiClient(): BinanceApiRestClient? {
        return BinanceApiClientFactory.newInstance(DomainType.Com).newRestClient(DomainType.Com)
    }

    @Bean(name = ["clock"])
    fun getClock() : Clock{
        return Clock.system(ZoneId.of("GMT-3"))
    }
}