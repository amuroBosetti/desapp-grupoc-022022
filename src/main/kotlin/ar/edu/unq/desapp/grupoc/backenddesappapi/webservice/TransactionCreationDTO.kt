package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import javax.validation.constraints.NotBlank

data class TransactionCreationDTO(
    @field: NotBlank
    val symbol: String,
    val intendedPrice: Double,
    val operationType: OperationType
)
