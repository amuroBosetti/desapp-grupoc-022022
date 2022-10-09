package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource


class UserTest {
    val userRegistry = UserRegistry()

    @Test
    fun `when a user has been registered successfully, then they can be found in the user registry`() {
        val email = "user@unq.edu.ar"
        val user = BrokerUser(email, "Pepe", "Argento", "Calle falsa 123", "Abcdef!", "8340632811100092378329", "12345678")

        userRegistry.register(user)

        assertThat(userRegistry.findUserWithEmail(email)).isEqualTo(user)
    }

    @ParameterizedTest
    @ValueSource(strings = ["usuario", "usuario@un", "usuario@unq", "" ])
    fun `when a user tries to register with an invalid email format, then it fails`(invalidEmail : String) {
        assertThatThrownBy { BrokerUser(
            invalidEmail,
            "Pepe",
            "Argento",
            "Calle falsa 123",
            "Abcdef!",
            "8340632811100092378329",
            "12345678"
        ) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Invalid email")
    }

}
