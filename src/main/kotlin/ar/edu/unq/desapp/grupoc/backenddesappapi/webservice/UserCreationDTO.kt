package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import org.springframework.validation.annotation.Validated
import javax.validation.constraints.*
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "create user")
@Validated
class UserCreationDTO(
    @field:NotBlank
    @field:Size(min = 3, max = 30)
    val name: String,
    @field:NotBlank
    @field:Size(min = 3, max = 30)
    val surname: String,
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    @field:Size(min = 10, max = 30)
    val address: String,
    @field:NotBlank
    val password: String,
    val cvu: String,
    val walletId: String
) {

}
