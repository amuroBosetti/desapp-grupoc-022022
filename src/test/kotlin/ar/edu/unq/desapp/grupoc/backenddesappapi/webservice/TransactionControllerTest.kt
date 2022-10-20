package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionNotFoundException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionWithSameUserInBothSidesException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionAction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.TransactionService
import ar.edu.unq.desapp.grupoc.backenddesappapi.utils.TransactionFixture
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream

private const val NON_EXISTING_USER = "nonexistinguser@gmail.com"
private const val EXISTING_USER = "registereduser@gmail.com"
private val CREATED_OPERATION_ID = UUID.randomUUID()

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var transactionService: TransactionService

    @BeforeEach
    internal fun setUp() {
        every { transactionService.createTransaction(NON_EXISTING_USER, any()) }
            .throws(NotRegisteredUserException("User with email $NON_EXISTING_USER does not exist"))

        every { transactionService.createTransaction(EXISTING_USER, any()) }
            .returns(TransactionCreationResponseDTO(CREATED_OPERATION_ID, "", 0.0, OperationType.BUY))
    }

    @Test
    fun `when a not registered user tries to create a transaction, then it fails with an unauthorized error`() {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("user", NON_EXISTING_USER)
                .content(validPayload())
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `when a request is handled without a user header, then it fails with a bad request error`() {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload())
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `when a registered user tries to create a transaction with an empty body, then it fails with a bad request error`() {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("user", EXISTING_USER)
                .content("{}")
        ).andExpect(status().isBadRequest)
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    fun `when a registered user tries to create a transaction with an invalid body, then it fails with a bad request error`(
        invalidBody: String
    ) {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("user", EXISTING_USER)
                .content(invalidBody)
        ).andExpect(status().isBadRequest)
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

    @Test
    fun `when all active transactions are requested but there are none, an empty list is returned`() {
        mockNoActiveTransactionsResponse()

        val response = mockMvc.perform(
            get("/transaction/active")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val responseList = jacksonObjectMapper().readerForListOf(ActiveTransactionDTO::class.java)
            .readValue<List<ActiveTransactionDTO>>(response)
        assertThat(responseList).isEmpty()
    }

    @Test
    fun `when all active transactions are requested and there is one, it is returned`() {
        val transaction = mockOneActiveTransactionsResponse()

        val response = mockMvc.perform(
            get("/transaction/active")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val jsonMapper = JsonMapper.builder().addModule(JavaTimeModule()).build()
        val responseList = jsonMapper.readValue<List<ActiveTransactionDTO>>(response)
        assertThat(responseList).singleElement().satisfies(
            Consumer { it: ActiveTransactionDTO ->
                run {
                    assertThat(it.transactionId).isEqualTo(transaction.id!!)
                    assertThat(it.ownerId).isEqualTo(transaction.firstUser.id!!)
                    assertThat(it.symbol).isEqualTo(transaction.symbol)
                    assertThat(it.createdAt).isEqualTo(transaction.createadAt)
                    assertThat(it.intendedPrice).isEqualTo(transaction.intendedPrice)

                }
            }
        )
    }

    @Test
    fun `when a transaction is processed but the request is from the user who created the transaction, then it fails`() {
        every { transactionService.processTransaction(any(), any(), any()) }.throws(
            TransactionWithSameUserInBothSidesException(
                CREATED_OPERATION_ID
            )
        )

        mockMvc.perform(
            put("/transaction/{id}", CREATED_OPERATION_ID.toString())
                .header("user", EXISTING_USER)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when an inexisting transaction is processed, then it fails`() {
        every { transactionService.processTransaction(any(), any(), any()) }.throws(TransactionNotFoundException(CREATED_OPERATION_ID))

        mockMvc.perform(
            put("/transaction/{id}", CREATED_OPERATION_ID.toString())
                .header("user", EXISTING_USER)
                .content(jacksonObjectMapper().writeValueAsString(TransactionUpdateRequestDTO(TransactionAction.INFORM_TRANSFER)))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `when a transaction is processed successfully, then it is returned in its new status`() {
        val transactionStatus = TransactionStatus.WAITING_CONFIRMATION
        every { transactionService.processTransaction(any(), any(), any()) }.returns(
            TransactionFixture.aTransaction(
                EXISTING_USER,
                transactionStatus
            )
        )

        val stringResponse = mockMvc.perform(
            put("/transaction/{id}", CREATED_OPERATION_ID.toString())
                .header("user", EXISTING_USER)
                .content(jacksonObjectMapper().writeValueAsString(TransactionUpdateRequestDTO(TransactionAction.INFORM_TRANSFER)))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk).andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue<TransactionUpdateResponseDTO>(stringResponse)
        assertThat(responseDTO.status).isEqualTo(transactionStatus)
    }

    private fun mockOneActiveTransactionsResponse(): Transaction {
        val transaction = TransactionFixture.aTransaction(EXISTING_USER)
        every { transactionService.getActiveTransactions() }
            .returns(listOf(transaction))
        return transaction
    }

    private fun mockNoActiveTransactionsResponse() {
        every { transactionService.getActiveTransactions() }
            .returns(listOf())
    }

    private fun validPayload() =
        jacksonObjectMapper().writeValueAsString(TransactionCreationDTO("BNBUSDT", 0.0, OperationType.SELL))

    companion object {
        @JvmStatic
        fun invalidBodies(): Stream<String> {
            return Stream.of(
                """ { "symbol": "", "intendedPrice": 0.0, "operationType": "BUY" } """,
                """ { "symbol": "ALICEUSDT", "intendedPrice": , "operationType": "BUY" } """,
                """ { "symbol": "ALICEUSDT", "intendedPrice": 0.0, "operationType": "WEA" } """,
            )
        }
    }
}