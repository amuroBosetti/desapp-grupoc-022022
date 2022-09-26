package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest {

    @Autowired
    lateinit var service: UserService
    @Autowired
    lateinit var repository: UserRepository

    @Test
    fun `when a user is created, then it can be retrieved from the database`() {
        val user = UserCreationDTO(
            "pepe",
            "argento",
            "pepe.argento@gmail.com",
            "calle falsa 123, bajo flores",
            "Pepe1234",
            "2377658811100070538028",
            "12345678"
        )

        val createdUser = service.createUser(user)

        assertThat(repository.findById(createdUser.id!!)).hasValueSatisfying {
            assertThat(it).usingRecursiveComparison().isEqualTo(createdUser)
        }
    }
}