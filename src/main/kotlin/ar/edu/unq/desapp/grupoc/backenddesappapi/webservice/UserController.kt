package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.User
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.util.*
import javax.validation.Valid

@Controller
class UserController {

    @Autowired
    lateinit var userService: UserService

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(exception: MethodArgumentNotValidException) : ResponseEntity<String>{
        val messages = exception.fieldErrors.map {
            "Field ${it.field} ${it.defaultMessage}"
        }
        return ResponseEntity(messages.toString(),HttpStatus.BAD_REQUEST)
    }

    @RequestMapping("/user", method = [RequestMethod.POST])
    fun createUser(@Valid @RequestBody userCreationDTO : UserCreationDTO) : ResponseEntity<UserCreationResponseDTO> {
        val createdUser : User = userService.createUser(userCreationDTO)

        val response = UserCreationResponseDTO(
            createdUser.name,
            createdUser.surname,
            createdUser.email,
            createdUser.address,
            createdUser.walletId,
            createdUser.cvu,
            UUID.randomUUID()
        )

        return ResponseEntity(response, HttpStatus.CREATED)
    }

}