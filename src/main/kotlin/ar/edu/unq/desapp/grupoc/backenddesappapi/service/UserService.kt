package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService() {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    fun createUser(userCreationDTO: UserCreationDTO): BrokerUser {
        val encodedPassword = passwordEncoder.encode(userCreationDTO.password)
        val newUser = BrokerUser(
            userCreationDTO.email,
            userCreationDTO.name,
            userCreationDTO.surname,
            userCreationDTO.address,
            encodedPassword,
            userCreationDTO.cvu,
            userCreationDTO.walletId,
        )
        return userRepository.save(newUser)
    }

}
