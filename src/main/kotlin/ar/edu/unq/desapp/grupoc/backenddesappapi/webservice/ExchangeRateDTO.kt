package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import javax.validation.constraints.NotBlank

data class ExchangeRateDTO(
    @field: NotBlank
    val compra: String,
    @field: NotBlank
    val venta: String,
    @field: NotBlank
    val fecha: String
)
