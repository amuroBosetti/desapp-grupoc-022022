package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import javax.validation.constraints.NotBlank

data class TradedVolumeRequestDTO(
    @field: NotBlank val startingDate: String,
    @field: NotBlank val endingDate: String,
)

data class TradedVolumeResponseDTO(
    @field: NotBlank val startingDate: String,
    @field: NotBlank val endingDate: String,
    @field: NotBlank val amountInUSD: Double,
    @field: NotBlank val amountInARS: Double
)