package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.User
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationDTO
import org.springframework.stereotype.Service

@Service
class UserService {
    fun createUser(userCreationDTO: UserCreationDTO): User {
        val newUser = User(
            userCreationDTO.email,
            userCreationDTO.name,
            userCreationDTO.surname,
            userCreationDTO.address,
            userCreationDTO.password,
            userCreationDTO.cvu,
            userCreationDTO.walletId,
        )
        return newUser
    }

}
