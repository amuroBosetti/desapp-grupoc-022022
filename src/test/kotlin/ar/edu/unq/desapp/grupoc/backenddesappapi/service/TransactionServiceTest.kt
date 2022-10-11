package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

private const val VALID_USER = "validuser@gmail.com"

private const val SYMBOL = "BNBUSDT"

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        val user = UserFixture.aUser(VALID_USER, "9506368711100060517136", "12345678", 5L)
        userRepository.save(user)
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

    private fun validCreationPayload() = TransactionCreationDTO(SYMBOL, 15.0, OperationType.BUY)
}
