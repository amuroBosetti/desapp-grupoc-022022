package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun springOpenAPI() : OpenAPI {
        return OpenAPI()
                .info(Info().title("desapp-grupoc-022022 API")
                .description("Grupo C - Mauro Bosetti | Ignacio Robledo")
                .version("v1.0"))
    }

}