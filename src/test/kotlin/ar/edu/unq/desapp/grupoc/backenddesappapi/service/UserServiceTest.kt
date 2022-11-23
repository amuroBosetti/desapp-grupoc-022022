package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.security.JWTProvider
import ar.edu.unq.desapp.grupoc.backenddesappapi.security.UserAuthAttempt
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.UserCreationDTO
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class UserServiceTest {

    @Autowired
    private lateinit var jwtProvider: JWTProvider
    @Autowired
    private lateinit var service: UserService
    @Autowired
    private lateinit var repository: UserRepository

    @Test
    fun `when a user is created, then it can be retrieved from the database`() {
        val user = userCreationDTO("pepe.argento@gmail.com", "2377658811100070538028", "12345678")

        val createdUser = service.createUser(user)

        assertThat(repository.findById(createdUser.id!!)).hasValueSatisfying {
            assertThat(it).usingRecursiveComparison().ignoringFields("EMAIL_REGEX", "password").isEqualTo(createdUser)
        }
    }

    @Nested
    @DisplayName("given an already registered user")
    open inner class SellOperationTypeAlreadyExpressed {

        private lateinit var alreadyCreatedUser : BrokerUser
        private lateinit var password : String

        @BeforeEach
        internal fun setUp() {
            val userCreationDTO = userCreationDTO("moni.argento@gmail.com", "2377658811150070538028", "12345672")
            password = userCreationDTO.password
            alreadyCreatedUser = service.createUser(userCreationDTO)
        }
        @Test
        @Transactional
        internal open fun `when a login is attempted with the right credentials, then a valid token is returned`() {
            val userAuthAttempt = UserAuthAttempt(alreadyCreatedUser.email, password)

            val tokenDTO = service.login(userAuthAttempt)

            assertThat(jwtProvider.isValid(tokenDTO.token)).isTrue
        }

        @Test
        @Transactional
        internal open fun `when a login is attempted with the wrong credentials, then it fails`() {
            val userAuthAttempt = UserAuthAttempt(alreadyCreatedUser.email, "wrongPassword1234")

            assertThatThrownBy { service.login(userAuthAttempt) }
                .isInstanceOf(BadCredentialsException::class.java)
                .hasMessage("Bad credentials")
        }
    }

    @Transactional
    @Test
    internal fun `when a non existing user tries to log in, then it fails`() {
        val userAuthAttempt = UserAuthAttempt("nonexisting@gmail.com", "wrongPassword1234")

        assertThatThrownBy { service.login(userAuthAttempt) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessage("Bad credentials")
    }

    private fun userCreationDTO(email: String, cvu: String, walletId: String): UserCreationDTO {
        return UserCreationDTO(
            "pepe",
            "argento",
            email,
            "calle falsa 123, bajo flores",
            "Pepe1234",
            cvu,
            walletId
        )
    }
}