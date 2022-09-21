package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import java.util.*

data class UserCreationResponseDTO(
    val name: String,
    val surname: String,
    val email: String,
    val address: String,
    val walletId: String,
    val cvu: String,
    val userId: UUID
)