package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import PriceOutsidePriceBandException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.*
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.*
import ar.edu.unq.desapp.grupoc.backenddesappapi.security.JWTProvider
import ar.edu.unq.desapp.grupoc.backenddesappapi.security.UserAuthAttempt
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.TransactionService
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.UserService
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
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream

private const val NON_EXISTING_USER = "nonexistinguser@gmail.com"
private const val EXISTING_USER = "registereduser@gmail.com"
private const val PASSWORD: String = "Pepita1234"

private val CREATED_OPERATION_ID = UUID.randomUUID()


@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtProvider: JWTProvider

    @Autowired
    private lateinit var userService: UserService

    @MockkBean
    lateinit var transactionService: TransactionService

    @BeforeEach
    internal fun setUp() {
        val user = UserCreationDTO("pepe", "argento", EXISTING_USER, "calle falsa 123", PASSWORD, "1111111111111111111111", "12345678")
        userService.createUser(user)

        every { transactionService.createTransaction(NON_EXISTING_USER, any()) }
            .throws(NotRegisteredUserException("User with email $NON_EXISTING_USER does not exist"))

        every { transactionService.createTransaction(EXISTING_USER, any()) }
            .returns(TransactionCreationResponseDTO(CREATED_OPERATION_ID, "BNBUSDT", 0.0, OperationType.BUY, 4))
    }

    @Transactional
    @Test
    fun `when a not registered user tries to create a transaction, then it fails with an unauthorized error`() {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "noxExistingUserToken")
                .content(jacksonObjectMapper().writeValueAsString(validPayload()))
        ).andExpect(status().isUnauthorized)
    }

    @Transactional
    @Test
    fun `when a request is handled without an authorization  header, then it fails with an unauthorized error`() {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(validPayload()))
        ).andExpect(status().isUnauthorized)
    }

    @Transactional
    @Test
    fun `when a registered user tries to create a transaction with an empty body, then it fails with a bad request error`() {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
                .content("{}")
        ).andExpect(status().isBadRequest)
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("invalidBodies")
    fun `when a registered user tries to create a transaction with an invalid body, then it fails with a bad request error`(
        invalidBody: String
    ) {
        mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
                .content(invalidBody)
        ).andExpect(status().isBadRequest)
    }

    @Transactional
    @Test
    fun `when a registered user creates a transaction, then it is returned`() {
        val payload = validPayload()
        val response = mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
                .content(jacksonObjectMapper().writeValueAsString(payload))
        ).andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue(response, TransactionCreationResponseDTO::class.java)
        assertThat(responseDTO.operationId).isEqualTo(CREATED_OPERATION_ID)
        assertThat(responseDTO.symbol).isEqualTo(payload.symbol)
        assertThat(responseDTO.intendedPrice).isEqualTo(payload.intendedPrice)
        assertThat(responseDTO.quantity).isEqualTo(payload.quantity)
    }

    @Transactional
    @Test
    fun `when a registered user creates a buy transaction, then the crypto wallet id is included`() {
        val walletId = "12345678"
        every { transactionService.createTransaction(EXISTING_USER, any()) }
            .returns(
                TransactionCreationResponseDTO(
                    CREATED_OPERATION_ID,
                    "",
                    0.0,
                    OperationType.BUY,
                    0,
                    walletId = walletId,
                )
            )
        val payload = jacksonObjectMapper().writeValueAsString(
            TransactionCreationDTO(
                "BNBUSDT",
                0.0,
                OperationType.BUY,
                0,
                walletId = walletId,
            )
        )

        val response = mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
                .content(payload)
        ).andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue(response, TransactionCreationResponseDTO::class.java)
        assertThat(responseDTO.walletId).isEqualTo(walletId)
    }

    @Transactional
    @Test
    fun `when a registered user creates a sell transaction, then the cvu is included`() {
        val cvu = "4444444444444444444444"
        every { transactionService.createTransaction(EXISTING_USER, any()) }
            .returns(TransactionCreationResponseDTO(CREATED_OPERATION_ID, "", 0.0, OperationType.BUY, 0, cvu = cvu))
        val payload = jacksonObjectMapper().writeValueAsString(
            TransactionCreationDTO(
                "BNBUSDT",
                0.0,
                OperationType.BUY,
                0,
                cvu = cvu
            )
        )

        val response = mockMvc.perform(
            post("/transaction")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
                .content(payload)
        ).andExpect(status().isCreated)
            .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readValue(response, TransactionCreationResponseDTO::class.java)
        assertThat(responseDTO.cvu).isEqualTo(cvu)
    }

    @Transactional
    @Test
    fun `when a transaction is created with missing input, then it fails`() {
        val exceptionMessage = "A message"
        every { transactionService.createTransaction(any(), any()) }.throws(
            (UnexpectedUserInformationException(exceptionMessage))
        )

        val returnValue = mockMvc.perform(
            post("/transaction")
                .header("authorization", authTokenForUser(EXISTING_USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(validPayload()))
        )
            .andExpect(status().isBadRequest)
            .andReturn().response.contentAsString

        assertThat(returnValue).isEqualTo(exceptionMessage)
    }

    @Transactional
    @Test
    fun `when a transaction is created but intended price is outside price band, then it fails`() {
        val exceptionMessage = "Cannot express a transaction intent with a price 5 higher than the latest quotation"
        every { transactionService.createTransaction(any(), any()) }.throws(
            (PriceOutsidePriceBandException(exceptionMessage))
        )

        val returnValue = mockMvc.perform(
            post("/transaction")
                .header("authorization", authTokenForUser(EXISTING_USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(validPayload()))
        )
            .andExpect(status().isUnprocessableEntity)
            .andReturn().response.contentAsString

        assertThat(returnValue).isEqualTo(exceptionMessage)
    }

    @Transactional
    @Test
    fun `when all active transactions are requested but there are none, an empty list is returned`() {
        mockNoActiveTransactionsResponse()

        val response = mockMvc.perform(
            get("/transaction/active")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val responseList = jacksonObjectMapper().readerForListOf(ActiveTransactionDTO::class.java)
            .readValue<List<ActiveTransactionDTO>>(response)
        assertThat(responseList).isEmpty()
    }

    @Transactional
    @Test
    fun `when all active transactions are requested and there is one, it is returned`() {
        val transaction = mockOneActiveTransactionsResponse()

        val response = mockMvc.perform(
            get("/transaction/active")
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", authTokenForUser(EXISTING_USER))
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

    @Transactional
    @Test
    fun `when a transaction is processed but the request is from the user who created the transaction, then it fails`() {
        every { transactionService.processTransaction(any(), any(), any()) }.throws(
            TransactionWithSameUserInBothSidesException(
                CREATED_OPERATION_ID
            )
        )

        mockMvc.perform(
            put("/transaction/{id}", CREATED_OPERATION_ID.toString())
                .header("authorization", authTokenForUser(EXISTING_USER))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Transactional
    @Test
    fun `when a transaction is processed but user is not authorized to perform the action, then it fails`() {
        every { transactionService.processTransaction(any(), any(), any()) }.throws(
            UnauthorizedUserForAction(EXISTING_USER, TransactionAction.ACCEPT, CREATED_OPERATION_ID)
        )

        mockMvc.perform(
            put("/transaction/{id}", CREATED_OPERATION_ID.toString())
                .header("authorization", authTokenForUser(EXISTING_USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(TransactionUpdateRequestDTO(TransactionAction.ACCEPT)))
        )
            .andExpect(status().isUnprocessableEntity)
    }

    @Transactional
    @Test
    fun `when an inexisting transaction is processed, then it fails`() {
        every { transactionService.processTransaction(any(), any(), any()) }.throws(
            TransactionNotFoundException(
                CREATED_OPERATION_ID
            )
        )

        mockMvc.perform(
            put("/transaction/{id}", CREATED_OPERATION_ID.toString())
                .header("authorization", authTokenForUser(EXISTING_USER))
                .content(jacksonObjectMapper().writeValueAsString(TransactionUpdateRequestDTO(TransactionAction.INFORM_TRANSFER)))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Transactional
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
                .header("authorization", authTokenForUser(EXISTING_USER))
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

    private fun authTokenForUser(userEmail: String) : String {
        return jwtProvider.createToken(UserAuthAttempt(userEmail, PASSWORD))
    }

    private fun mockNoActiveTransactionsResponse() {
        every { transactionService.getActiveTransactions() }
            .returns(listOf())
    }

    private fun validPayload() =
        TransactionCreationDTO(
            "BNBUSDT",
            0.0,
            OperationType.SELL,
            4,
            "12345678"
        )

    companion object {
        @JvmStatic
        fun invalidBodies(): Stream<String> {
            return Stream.of(
                """ { "symbol": "", "intendedPrice": 0.0, "operationType": "BUY", "quantity": 4 } """,
                """ { "symbol": "ALICEUSDT", "intendedPrice": , "operationType": "BUY", "quantity": 4} """,
                """ { "symbol": "ALICEUSDT", "intendedPrice": 0.0, "operationType": "WEA", "quantity": 4 } """,
                """ { "symbol": "ALICEUSDT", "intendedPrice": 0.0, "operationType": "WEA", "quantity": } """,
            )
        }
    }
}