package ar.edu.unq.desapp.grupoc.backenddesappapi

import ar.edu.unq.desapp.grupoc.backenddesappapi.service.DBInitializerService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
//@EnableScheduling
class BackendDesappApiApplication

fun main(args: Array<String>) {
    runApplication<BackendDesappApiApplication>(*args)
}
