package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

val userRegistry: UserRegistry = UserRegistry()

class UserTest {

    @Test
    fun `cuando un usuario es registrado exitosamente, luego puede ser encontrado en el registro de usuarios`() {
        val email = "user@unq.edu.ar"
        val user = User(email, "Pepe", "Argento", "Calle falsa 123", "Abcdef!", "8340632811100092378329", "12345678")

        userRegistry.register(user)

        assertThat(userRegistry.findUserWithEmail(email)).isEqualTo(user)
    }

    @ParameterizedTest
    @ValueSource(strings = ["usuario", "usuario@un", "usuario@unq", "" ])
    fun `cuando un usuario es registrado con un formato de email invalido, luego se levanta una excepcion`(invalidEmail : String) {
        assertThatThrownBy { User(
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
