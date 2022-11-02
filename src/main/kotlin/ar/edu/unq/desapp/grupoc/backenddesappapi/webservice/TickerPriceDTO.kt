package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import javax.validation.constraints.NotBlank


data class TickerPriceDTO(
    @field: NotBlank val symbol: String,
    @field: NotBlank val price: String,
)