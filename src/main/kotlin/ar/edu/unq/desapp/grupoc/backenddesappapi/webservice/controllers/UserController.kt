package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.security.UserAuthAttempt
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.UserService
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationResponseDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.dto.TokenDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "Users", description = "User registration service")
@Controller
class UserController {

    @Autowired
    lateinit var userService: UserService

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(exception: MethodArgumentNotValidException): ResponseEntity<String> {
        val messages = exception.fieldErrors.map {
            "Field ${it.field} ${it.defaultMessage}"
        }
        return ResponseEntity(messages.toString(), HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException::class)
    fun handleException(exception: BadCredentialsException) : ResponseEntity<String>{
        return ResponseEntity("Bad credentials", HttpStatus.UNAUTHORIZED)
    }

    @Operation(summary = "Register a new user")
    @RequestMapping("/user", method = [RequestMethod.POST])
    fun createUser(@Valid @RequestBody userCreationDTO: UserCreationDTO): ResponseEntity<UserCreationResponseDTO> {
        val createdUser: BrokerUser = userService.createUser(userCreationDTO)

        val response = UserCreationResponseDTO(
            createdUser.name,
            createdUser.surname,
            createdUser.email,
            createdUser.address,
            createdUser.walletId,
            createdUser.cvu,
            createdUser.id
        )

        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @Operation(summary = "Login")
    @RequestMapping("/login", method = [RequestMethod.POST])
    fun login(@Valid @RequestBody authAttempt: UserAuthAttempt) : ResponseEntity<TokenDTO> {
        val returnedToken : TokenDTO = userService.login(authAttempt)

        return ResponseEntity(returnedToken, HttpStatus.OK)
    }


}