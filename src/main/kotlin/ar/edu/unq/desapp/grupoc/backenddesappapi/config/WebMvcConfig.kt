package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer{

    @Autowired
    private lateinit var authInterceptor: AuthInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .excludePathPatterns("/**/token/**")
            .excludePathPatterns("/**/user/**")
            .excludePathPatterns("/**/login/**")
            .excludePathPatterns("/**/swagger-ui/**")
            .excludePathPatterns("/**/api-docs/**")
    }

}