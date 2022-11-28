package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

open class HttpController {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<String> {
        return ResponseEntity("There was an unexpected error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
