package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val VALID_USER = "validuser@gmail.com"

private const val SYMBOL = "BNBUSDT"

@SpringBootTest
class TransactionServiceTest {

    private val transactionId = UUID.randomUUID()

    @Autowired
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockkBean
    private lateinit var transactionRepository : TransactionRepository

    @BeforeEach
    fun setUp() {
        val user = UserFixture.aUser(VALID_USER)
        userRepository.save(user)

        val transaction = Transaction(user, OperationType.SELL, 10.0, "BNBUSDT")
        transaction.id = transactionId
        every { transactionRepository.save(any()) }.returns(transaction)
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
    fun `when a transaction is created, then it is returned`(){
        val validCreationPayload = validCreationPayload()

        val response = transactionService.createTransaction(VALID_USER, validCreationPayload)

        assertThat(response.operationId).isEqualTo(transactionId)
        assertThat(response.symbol).isEqualTo(SYMBOL)
        assertThat(response.intendedPrice).isEqualTo(validCreationPayload.intendedPrice)
        assertThat(response.operationType).isEqualTo(validCreationPayload.operationType)
    }

    private fun validCreationPayload() = TransactionCreationDTO(SYMBOL, 10.0, OperationType.SELL)
}
