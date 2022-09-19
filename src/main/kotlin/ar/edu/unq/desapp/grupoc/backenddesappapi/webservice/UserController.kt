package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import javax.validation.Valid

@Controller
class UserController {

    @RequestMapping("/user", method = [RequestMethod.POST])
    fun createUser(@Valid @RequestBody userCreationDTO : UserCreationDTO) : ResponseEntity<String> {
        return ResponseEntity(HttpStatus.CREATED)
    }

}