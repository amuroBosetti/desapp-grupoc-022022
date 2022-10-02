package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import javax.validation.constraints.NotBlank

data class TransactionCreationDTO(
    @field: NotBlank
    val symbol : String
    )
