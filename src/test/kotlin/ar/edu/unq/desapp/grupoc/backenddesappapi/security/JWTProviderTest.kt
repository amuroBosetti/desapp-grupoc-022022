package ar.edu.unq.desapp.grupoc.backenddesappapi.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.BadCredentialsException

@SpringBootTest
internal class JWTProviderTest {

    private val aPassword: String = "aguanteLaMasa1234"
    private val anEmail: String = "vicente.viloni@gmail.com"

    @Autowired
    private lateinit var jwtProvider: JWTProvider

    @Test
    fun `when a token is created, then it can be verified`() {
        val authAttempt = UserAuthAttempt(anEmail, aPassword)

        val createdToken = jwtProvider.createToken(authAttempt)

        assertThat(jwtProvider.isValid(createdToken)).isTrue
    }

    @Test
    fun `when an invalid token is verified, then it is invalid`() {
        val invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        assertThat(jwtProvider.isValid(invalidToken)).isFalse
    }

    @Test
    fun `when a valid token is created, then the user email can be retrieved from it`() {
        val authAttempt = UserAuthAttempt(anEmail, aPassword)

        val createdToken = jwtProvider.createToken(authAttempt)

        assertThat(jwtProvider.getEmailFromToken(createdToken)).isEqualTo(anEmail)
    }

    @Test
    fun `when user email is retrieved from an invalid token, then it fails`() {
        val invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

        assertThatThrownBy { jwtProvider.getEmailFromToken(invalidToken) }
            .isInstanceOf(BadCredentialsException::class.java)
            .hasMessage("Bad token")
    }
}