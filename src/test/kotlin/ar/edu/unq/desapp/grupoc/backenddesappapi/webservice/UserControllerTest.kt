package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.security.UserAuthAttempt
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.UserService
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.dto.TokenDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private val userId = 123L

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        val user = BrokerUser(
            "pepe@gmail.com",
            "pepe",
            "argento",
            "calle falsa 1234",
            "password12345",
            "7987818411100011451153",
            "12345678"
        )
        user.id = userId
        every { userService.createUser(any()) } returns user
    }

    @Test
    fun `when a POST to user is handled without body, a bad request error is returned`() {
        mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when a POST to user is handled with an invalid password, a bad request error is returned`() {
        val userCreationPayload = UserCreationDTO(
            "pepe",
            "argento",
            "pepe@gmail.com",
            "calle falsa 1234",
            "",
            "7987818411100011451153",
            "12345678"
        )
        val userCreationJSON = mapToJSON(userCreationPayload)
        val errorMessage =
            mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(userCreationJSON))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString
        assertThat(errorMessage).contains("Field password must not be blank")
    }


    @Test
    fun `when a POST to user is handled with an invalid address, a bad request error is returned`() {
        val userCreationPayload = UserCreationDTO(
            "pepe",
            "argento",
            "pepe@gmail.com",
            "",
            "password12345",
            "7987818411100011451153",
            "12345678"
        )
        val userCreationJSON = mapToJSON(userCreationPayload)
        val errorMessage =
            mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(userCreationJSON))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString
        assertThat(errorMessage).contains(
            "Field address",
            "size must be between 10 and 30",
            "must not be blank"
        )
    }

    @Test
    fun `when a POST to user is handled with an invalid email, a bad request error is returned`() {
        val userCreationPayload = UserCreationDTO(
            "pepe",
            "argento",
            "invalid_not_blank",
            "calle falsa 1234",
            "password12345",
            "7987818411100011451153",
            "12345678"
        )
        val userCreationJSON = mapToJSON(userCreationPayload)
        val errorMessage =
            mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(userCreationJSON))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString
        assertThat(errorMessage).contains(
            "Field email",
            "must be a well-formed email address"
        )
    }

    @Test
    fun `when a POST to user is handled with an invalid name or surname, a bad request error is returned`() {
        val userCreationPayload = UserCreationDTO(
            "p",
            "a",
            "pepe@gmail.com",
            "calle falsa 1234",
            "password12345",
            "7987818411100011451153",
            "12345678"
        )
        val userCreationJSON = mapToJSON(userCreationPayload)
        val errorMessage =
            mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(userCreationJSON))
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString
        assertThat(errorMessage).contains(
            "Field name",
            "Field surname",
            "size must be between 3 and 30"
        )
    }

    @Test
    fun `when a POST to user is handled, then a created status and the created user are returned`() {
        val userCreationPayload = UserCreationDTO(
            "pepe",
            "argento",
            "pepe@gmail.com",
            "calle falsa 1234",
            "123453645756",
            "7987818411100011451153",
            "12345678"
        )
        val userCreationJSON = mapToJSON(userCreationPayload)

        val response = mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(userCreationJSON))
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue(response, UserCreationResponseDTO::class.java)
        assertThat(responseDTO).usingRecursiveComparison().ignoringFields("userId", "password")
            .isEqualTo(userCreationPayload)
        assertThat(responseDTO.userId).isEqualTo(userId)
        assertThat(responseDTO.name).isNotNull
    }

    @Test
    fun `when a POST to login is handled with correct credentials, then the token DTO is returned`() {
        val authAttempt = UserAuthAttempt(
            "pepeargento@gmail.com",
            "Password123"
        )
        val token = "token"
        every { userService.login(authAttempt) }.returns(TokenDTO(token))
        val authAttemptJSON = mapToJSON(authAttempt)

        val response =
            mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(authAttemptJSON))
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue(response, TokenDTO::class.java)
        assertThat(responseDTO.token).isEqualTo(token)
    }

    @Test
    fun `when a POST to login is handled with incorrect credentials, then it returns an unauthorized error`() {
        every { userService.login(any()) }.throws(BadCredentialsException("Bad credentials"))
        val authAttemptJSON = mapToJSON(UserAuthAttempt("pepeargento@gmail.com", "Password123"))

        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(authAttemptJSON))
            .andExpect(status().isUnauthorized)
    }


    private fun mapToJSON(anyObject: Any): String =
        jacksonObjectMapper().writeValueAsString(anyObject)

}