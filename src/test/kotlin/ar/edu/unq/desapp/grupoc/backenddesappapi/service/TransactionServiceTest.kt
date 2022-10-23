package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.TickerPrice
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
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

    @MockkBean
    lateinit var client: BinanceApiRestClient

    @Autowired
    private lateinit var transactionService: TransactionService
    val mockPrice = validCreationPayload().intendedPrice.toString()
    val mockSymbol = SYMBOL

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        val user = UserFixture.aUser(VALID_USER, "9506368711100060517136", "12345678", 5L)
        userRepository.save(user)

        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice
        tickerPrice.symbol = mockSymbol
        every { client.getPrice(mockSymbol) } returns  tickerPrice
        every { client.getPrice("") } throws RuntimeException("Could not get the token price")
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

    private fun validCreationPayload(): TransactionCreationDTO {
        val intendedPrice = 15.0
        return TransactionCreationDTO(SYMBOL, intendedPrice, OperationType.BUY)
    }
}
