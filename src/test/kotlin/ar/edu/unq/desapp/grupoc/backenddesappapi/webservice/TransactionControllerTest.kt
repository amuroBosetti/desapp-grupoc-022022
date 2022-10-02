package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.TransactionService
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

private const val NON_EXISTING_USER = "nonexistinguser@gmail.com"
private const val EXISTING_USER = "registereduser@gmail.com"
private val CREATED_OPERATION_ID = UUID.randomUUID()

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    lateinit var mockMvc : MockMvc
    @MockkBean
    lateinit var transactionService: TransactionService

    @BeforeEach
    internal fun setUp() {
        every { transactionService.createTransaction(NON_EXISTING_USER, any()) }
            .throws(NotRegisteredUserException("User with email $NON_EXISTING_USER does not exist"))

        every { transactionService.createTransaction(EXISTING_USER, any()) }
            .returns(TransactionCreationResponseDTO(CREATED_OPERATION_ID))
    }

    @Test
    fun `when a not registered user tries to create a transaction, then it fails with an unauthorized error`() {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .header("user", NON_EXISTING_USER)
            .content(validPayload())
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `when a request is handled without a user header, then it fails with a bad request error`() {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validPayload())
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `when a registered user tries to create a transaction with an empty body, then it fails with a bad request error`() {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .header("user", EXISTING_USER)
            .content("{}")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `when a registered user tries to create a transaction with an invalid body, then it fails with a bad request error`() {
        val invalidBody = """
                { "symbol": "" }
            """

        val response = mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("user", EXISTING_USER)
                .content(
                    invalidBody
                )
        ).andExpect(status().isBadRequest).andReturn().response.contentAsString

        assertThat(response).contains("Field symbol must not be blank")
    }

    @Test
    fun `when a registered user creates a transaction, then it is returned`() {
        val response = mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("user", EXISTING_USER)
                .content(validPayload())
        ).andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue(response, TransactionCreationResponseDTO::class.java)
        assertThat(responseDTO.operationId).isEqualTo(CREATED_OPERATION_ID)
    }

    private fun validPayload() = jacksonObjectMapper().writeValueAsString(TransactionCreationDTO("BNBUSDT"))
}