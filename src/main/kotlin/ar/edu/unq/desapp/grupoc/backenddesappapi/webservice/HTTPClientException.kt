package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import org.springframework.http.HttpStatus

class HTTPClientException(val returnMessage: String, val status: HttpStatus) : RuntimeException()
