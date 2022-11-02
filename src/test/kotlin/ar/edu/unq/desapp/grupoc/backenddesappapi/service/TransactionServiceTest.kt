package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionNotFoundException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.UnauthorizedUserForAction
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.UnexpectedUserInformationException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionAction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.utils.TransactionFixture
import ar.edu.unq.desapp.grupoc.backenddesappapi.utils.TransactionFixture.Companion.A_WALLET_ID
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.ExchangeRateDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TradedVolumeResponseDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.TickerPrice
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.*


private const val VALID_USER = "validuser@gmail.com"
private const val ANOTHER_VALID_USER = "anothervaliduser@gmail.com"
private const val SYMBOL = "BNBUSDT"

@SpringBootTest
class TransactionServiceTest {

    @MockkBean
    lateinit var client: BinanceApiRestClient

    @Autowired
    private lateinit var transactionService: TransactionService
    val mockPrice = validCreationPayload(OperationType.BUY).intendedPrice.toString()
    val mockSymbol = SYMBOL

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @MockkBean
    lateinit var USDAPI: USDAPI

    @MockkBean
    lateinit var clock: Clock

    @BeforeEach
    fun setUp() {
        val user = userRepository.save(UserFixture.aUser(VALID_USER, "9506368711100060517136", "12345578"))
        userRepository.save(UserFixture.aUser(ANOTHER_VALID_USER, "8506368711100060517136", "82345678"))

        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice
        tickerPrice.symbol = mockSymbol
        every { client.getPrice(mockSymbol) } returns tickerPrice
        every { client.getPrice("") } throws RuntimeException("Could not get the token price")
        every { clock.instant() } returns Instant.parse("2022-10-01T09:15:00Z")
        every { clock.zone } returns ZoneId.of("GMT-3")
        every { USDAPI.getARSOfficialRate() } returns ExchangeRateDTO("150", "155", "2022-10-01")
    }

    @Nested
    @DisplayName("when a transaction is created")
    open inner class TransactionCreationTest {

        @Test
        @Transactional
        open fun `without a user is received, then it fails`() {
            val validCreationPayload = validCreationPayload(OperationType.BUY)

            assertThatThrownBy { transactionService.createTransaction("", validCreationPayload) }.isInstanceOf(
                RuntimeException::class.java
            ).hasMessage("User email cannot be blank")
        }

        @Test
        @Transactional
        open fun `but user does not exist, then it fails`() {
            val notRegisteredUserEmail = "notRegisteredUser@gmail.com"
            val validCreationPayload = validCreationPayload(OperationType.BUY)

            assertThatThrownBy {
                transactionService.createTransaction(
                    notRegisteredUserEmail,
                    validCreationPayload
                )
            }.isInstanceOf(NotRegisteredUserException::class.java)
                .hasMessage("User with email $notRegisteredUserEmail is not registered")
        }

        @Test
        @Transactional
        open fun `successfully, then it is returned`() {
            val validCreationPayload = validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)

            val response = transactionService.createTransaction(VALID_USER, validCreationPayload)

            assertThat(response.operationId).isNotNull
            assertThat(response.symbol).isEqualTo(SYMBOL)
            assertThat(response.intendedPrice).isEqualTo(validCreationPayload.intendedPrice)
            assertThat(response.operationType).isEqualTo(validCreationPayload.operationType)
        }

        @Test
        @Transactional
        open fun `of type buy, then the wallet id is included`() {
            val validCreationPayload = validCreationPayload(OperationType.BUY, walletId = "12345678")

            val response = transactionService.createTransaction(VALID_USER, validCreationPayload)

            assertThat(response.operationId).isNotNull
            assertThat(response.symbol).isEqualTo(SYMBOL)
            assertThat(response.intendedPrice).isEqualTo(validCreationPayload.intendedPrice)
            assertThat(response.operationType).isEqualTo(validCreationPayload.operationType)
            assertThat(response.walletId).isEqualTo(validCreationPayload.walletId)
        }

        @Test
        @Transactional
        open fun `of type sell, then the cvu is included`() {
            val validCreationPayload = validCreationPayload(OperationType.SELL, cvu = TransactionFixture.A_CVU)

            val response = transactionService.createTransaction(VALID_USER, validCreationPayload)

            assertThat(response.operationId).isNotNull
            assertThat(response.symbol).isEqualTo(SYMBOL)
            assertThat(response.intendedPrice).isEqualTo(validCreationPayload.intendedPrice)
            assertThat(response.operationType).isEqualTo(validCreationPayload.operationType)
            assertThat(response.cvu).isEqualTo(validCreationPayload.cvu)
        }

        @Test
        @Transactional
        open fun `with missing input, then it fails and is not created`() {
            val validCreationPayload = validCreationPayload(OperationType.SELL)

            assertThatThrownBy { transactionService.createTransaction(VALID_USER, validCreationPayload) }
                .isInstanceOf(UnexpectedUserInformationException::class.java)
                .hasMessage("Cannot create a SELL transaction with cvu null")

            assertThat(transactionRepository.findAll()).isEmpty()
        }

    }

    @Test
    @Transactional
    fun `when all active transactions are requested, then they are returned`() {
        val transaction = transactionService.createTransaction(
            VALID_USER,
            validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)
        )

        assertThat(transactionService.getActiveTransactions()).singleElement().extracting("id")
            .isEqualTo(transaction.operationId)
    }

    @Test
    @Transactional
    fun `when an unexisting transaction is processed, then it fails`() {
        val nonExistingTransactionId = UUID.randomUUID()
        assertThatThrownBy {
            transactionService.processTransaction(
                nonExistingTransactionId, ANOTHER_VALID_USER, TransactionAction.ACCEPT
            )
        }.isInstanceOf(TransactionNotFoundException::class.java)
            .hasMessage("Could not find transaction with id $nonExistingTransactionId")
    }

    @Test
    @Transactional
    fun `when a transaction is processed but the user does not exists, then it fails`() {
        val transaction =
            transactionService.createTransaction(VALID_USER, validCreationPayload(OperationType.BUY, A_WALLET_ID))
        val nonExistingUser = "nonExistingUser@gmail.com"

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId, nonExistingUser, TransactionAction.ACCEPT
            )
        }.isInstanceOf(NotRegisteredUserException::class.java)
            .hasMessage("User with email $nonExistingUser is not registered")
    }

    @Test
    @Transactional
    fun `when an active transaction is processed, then it is returned with its new status and second user is saved`() {
        val transaction = transactionService.createTransaction(
            VALID_USER,
            validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)
        )

        val processedTransaction = transactionService.processTransaction(
            transaction.operationId, ANOTHER_VALID_USER, TransactionAction.ACCEPT
        )

        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        assertThat(processedTransaction.secondUser?.email).isEqualTo(ANOTHER_VALID_USER)
    }

    @Test
    @Transactional
    fun `when an active transaction is processed but the action is not valid for status, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(
            VALID_USER,
            validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)
        )

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId, ANOTHER_VALID_USER, TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION
            )
        }.isInstanceOf(UnauthorizedUserForAction::class.java)

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.ACTIVE)
    }

    @Test
    @Transactional
    fun `when a transaction is processed by a user who is not part of it, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(
            VALID_USER,
            validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)
        )
        transactionService.processTransaction(transaction.operationId, ANOTHER_VALID_USER, TransactionAction.ACCEPT)
        val thirdUser =
            userRepository.save(UserFixture.aUser("thirduser@gmail.com", "8506368711100060514136", "82349678"))

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId, thirdUser.email, TransactionAction.INFORM_TRANSFER
            )
        }.isInstanceOf(UnauthorizedUserForAction::class.java)
            .hasMessage("User ${thirdUser.email} is not authorized to perform action ${TransactionAction.INFORM_TRANSFER} on transaction ${transaction.operationId}")

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
    }

    @Test
    @Transactional
    fun `when a pending transaction is processed by a user who does not have to inform a transfer, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(
            VALID_USER,
            validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)
        )
        transactionService.processTransaction(transaction.operationId, ANOTHER_VALID_USER, TransactionAction.ACCEPT)

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId,
                ANOTHER_VALID_USER,
                TransactionAction.INFORM_TRANSFER
            )
        }.isInstanceOf(UnauthorizedUserForAction::class.java)
            .hasMessage("User $ANOTHER_VALID_USER is not authorized to perform action ${TransactionAction.INFORM_TRANSFER} on transaction ${transaction.operationId}")

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
    }

    @Test
    @Transactional
    fun `when a waiting confirmation transaction is processed by a user who does not have to confirm a transfer reception, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(
            VALID_USER, validCreationPayload(
                OperationType.SELL, cvu = TransactionFixture.A_CVU
            )
        )
        transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            TransactionAction.INFORM_TRANSFER
        )

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId,
                ANOTHER_VALID_USER,
                TransactionAction.CONFIRM_TRANSFER_RECEPTION
            )
        }.isInstanceOf(UnauthorizedUserForAction::class.java)
            .hasMessage("User $ANOTHER_VALID_USER is not authorized to perform action ${TransactionAction.CONFIRM_TRANSFER_RECEPTION} on transaction ${transaction.operationId}")

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.WAITING_CONFIRMATION)
    }

    @Test
    @Transactional
    fun `when a pending crypto transfer transaction is processed by a user who does not have to inform crypto transfer, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(
            VALID_USER,
            validCreationPayload(OperationType.BUY, walletId = A_WALLET_ID)
        )
        transactionService.processTransaction(transaction.operationId, ANOTHER_VALID_USER, TransactionAction.ACCEPT)
        transactionService.processTransaction(transaction.operationId, VALID_USER, TransactionAction.INFORM_TRANSFER)
        transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            TransactionAction.CONFIRM_TRANSFER_RECEPTION
        )

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId,
                VALID_USER,
                TransactionAction.INFORM_CRYPTO_TRANSFER
            )
        }.isInstanceOf(UnauthorizedUserForAction::class.java)
            .hasMessage("User $VALID_USER is not authorized to perform action ${TransactionAction.INFORM_CRYPTO_TRANSFER} on transaction ${transaction.operationId}")

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING_CRYPTO_TRANSFER)
    }

    @Test
    @Transactional
    fun `when a waiting crypto transfer confirmation transaction is processed by a user who does not have to confirm crypto transfer reception, then it fails and the transaction is not processed`() {
        val transaction = transactionService.createTransaction(
            VALID_USER, validCreationPayload(
                OperationType.SELL, cvu = TransactionFixture.A_CVU
            )
        )
        transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            TransactionAction.INFORM_TRANSFER
        )
        transactionService.processTransaction(
            transaction.operationId,
            VALID_USER,
            TransactionAction.CONFIRM_TRANSFER_RECEPTION
        )
        transactionService.processTransaction(
            transaction.operationId,
            VALID_USER,
            TransactionAction.INFORM_CRYPTO_TRANSFER
        )

        assertThatThrownBy {
            transactionService.processTransaction(
                transaction.operationId,
                VALID_USER,
                TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION
            )
        }.isInstanceOf(UnauthorizedUserForAction::class.java)
            .hasMessage("User $VALID_USER is not authorized to perform action ${TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION} on transaction ${transaction.operationId}")

        val processedTransaction = transactionRepository.findById(transaction.operationId).get()
        assertThat(processedTransaction.status).isEqualTo(TransactionStatus.WAITING_CRYPTO_CONFIRMATION)
    }

    @Transactional
    @Test
    fun `when asked the traded volume in dollars in the same day and there are none then nothing was traded`() {
        val date = "2022-10-01"
        val tradedVolumeDTO: TradedVolumeResponseDTO = transactionService.getTradedVolume(date, date)

        assertThat(tradedVolumeDTO.amountInUSD).isEqualTo(0.00)
        assertThat(tradedVolumeDTO.amountInARS).isEqualTo(0.00)
    }

    @Transactional
    @Test
    fun `when asked the traded volume in dollars in the same day and there is one transaction then that was traded`() {
        val date = "2022-10-01"

        val transaction = transactionService.createTransaction(
            VALID_USER, validCreationPayload(
                OperationType.SELL, cvu = TransactionFixture.A_CVU
            )
        )
        transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            TransactionAction.INFORM_TRANSFER
        )
        transactionService.processTransaction(
            transaction.operationId,
            VALID_USER,
            TransactionAction.CONFIRM_TRANSFER_RECEPTION
        )
        transactionService.processTransaction(
            transaction.operationId,
            VALID_USER,
            TransactionAction.INFORM_CRYPTO_TRANSFER
        )
        transactionService.processTransaction(
            transaction.operationId,
            ANOTHER_VALID_USER,
            TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION
        )
        val tradedVolumeDTO: TradedVolumeResponseDTO = transactionService.getTradedVolume(date, date)

        assertThat(tradedVolumeDTO.amountInUSD).isEqualTo(75.00)
        assertThat(tradedVolumeDTO.amountInARS).isEqualTo(11250.0)
    }

    private fun validCreationPayload(operationType: OperationType, walletId: String? = null, cvu: String? = null) =
        TransactionCreationDTO(
            SYMBOL,
            15.0,
            operationType,
            5,
            walletId,
            cvu
        )

}
