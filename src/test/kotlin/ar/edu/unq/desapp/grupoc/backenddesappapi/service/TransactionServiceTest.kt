package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.InvalidTransactionStatusException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionNotFoundException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val VALID_USER = "validuser@gmail.com"
private const val ANOTHER_VALID_USER = "anothervaliduser@gmail.com"

private const val SYMBOL = "BNBUSDT"

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @BeforeEach
    fun setUp() {
        userRepository.save(UserFixture.aUser(VALID_USER, "9506368711100060517136", "12345578"))
        userRepository.save(UserFixture.aUser(ANOTHER_VALID_USER, "8506368711100060517136", "82345678"))
    }

    @Test
    @Transactional
    fun `when a transaction without a user is received, then it fails`() {
        val validCreationPayload = validCreationPayload()

        assertThatThrownBy { transactionService.createTransaction("", validCreationPayload) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("User email cannot be blank")
    }

    @Test
    @Transactional
    fun `when a transaction is received but user does not exist, then it fails`() {
        val notRegisteredUserEmail = "notRegisteredUser@gmail.com"
        val validCreationPayload = validCreationPayload()

        assertThatThrownBy { transactionService.createTransaction(notRegisteredUserEmail, validCreationPayload) }
            .isInstanceOf(NotRegisteredUserException::class.java)
            .hasMessage("User with email $notRegisteredUserEmail is not registered")
    }

    @Test
    @Transactional
    fun `when a transaction is created, then it is returned`(){
        val validCreationPayload = validCreationPayload()

        val response = transactionService.createTransaction(VALID_USER, validCreationPayload)

        assertThat(response.operationId).isNotNull
        assertThat(response.symbol).isEqualTo(SYMBOL)
        assertThat(response.intendedPrice).isEqualTo(validCreationPayload.intendedPrice)
        assertThat(response.operationType).isEqualTo(validCreationPayload.operationType)
    }

    @Test
    @Transactional
    fun `when all active transactions are requested, then they are returned`() {
        val transaction = transactionService.createTransaction(VALID_USER, validCreationPayload())

        assertThat(transactionService.getActiveTransactions()).singleElement().extracting("id")
            .isEqualTo(transaction.operationId)
    }

    @Test
    @Transactional
    fun `when an unexisting transaction is processed, then it fails`() {
        val nonExistingTransactionId = UUID.randomUUID()
        assertThatThrownBy { transactionService.processTransaction(
            nonExistingTransactionId,
            ANOTHER_VALID_USER,
            "anyAction"
        ) }
            .isInstanceOf(TransactionNotFoundException::class.java)
            .hasMessage("Could not find transaction with id $nonExistingTransactionId")
    }

    @Test
    @Transactional
    fun `when an active transaction is processed, then it is returned with its new status`() {
        val transaction = transactionService.createTransaction(VALID_USER, validCreationPayload())

        val processedTransaction = transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            "anyAction"
        )

        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        assertThat(processedTransaction.secondUser?.email).isEqualTo(ANOTHER_VALID_USER)
    }

    @Test
    @Transactional
    fun `when an active transaction is processed but the action is not valid for status, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(VALID_USER, validCreationPayload())

        assertThatThrownBy { transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            "anyAction"
        ) }
            .isInstanceOf(InvalidTransactionStatusException::class.java)

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        assertThat(processedTransaction.secondUser?.email).isEqualTo(ANOTHER_VALID_USER)
    }

    private fun validCreationPayload() = TransactionCreationDTO(SYMBOL, 15.0, OperationType.BUY)
}
