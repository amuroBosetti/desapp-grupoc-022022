package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class TransactionCreationDTO(
    @field: NotBlank
    val symbol: String,
    @field: NotNull
    val intendedPrice: Double,
    @field: NotNull
    val operationType: OperationType,
    val walletId: String? = null,
    val cvu: String? = null
)
