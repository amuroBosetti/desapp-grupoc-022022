package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
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
    @field:Size(min = 22, max = 22)
    val cvu: String,
    @field:Size(min = 8, max = 8)
    val walletId: String
) {
    override fun toString(): String {
        return "UserCreationDTO(name='$name', surname='$surname', email='$email', address='$address', cvu='$cvu', walletId='$walletId')"
    }
}
