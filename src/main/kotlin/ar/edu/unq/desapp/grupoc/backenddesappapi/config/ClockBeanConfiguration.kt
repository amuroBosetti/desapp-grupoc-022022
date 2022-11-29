package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class ClockBeanConfiguration {

    @Bean(name = ["clock"])
    fun getClock() : Clock {
        return Clock.system(ZoneId.of("GMT-3"))
    }
}