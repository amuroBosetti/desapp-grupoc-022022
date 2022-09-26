package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService() {

    @Autowired
    lateinit var userRepository: UserRepository

    fun createUser(userCreationDTO: UserCreationDTO): BrokerUser {
        val newUser = BrokerUser(
            userCreationDTO.email,
            userCreationDTO.name,
            userCreationDTO.surname,
            userCreationDTO.address,
            userCreationDTO.password,
            userCreationDTO.cvu,
            userCreationDTO.walletId,
        )
        return userRepository.save(newUser)
    }

}
