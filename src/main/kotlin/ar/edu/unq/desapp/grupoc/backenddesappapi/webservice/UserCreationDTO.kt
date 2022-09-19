package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Validated
class UserCreationDTO(
    val name: String,
    val surname: String,
    val email: String,
    val address: String,
    @field:NotBlank
    val password: String,
    val cvu: String,
    val walletId: String
) {

}
