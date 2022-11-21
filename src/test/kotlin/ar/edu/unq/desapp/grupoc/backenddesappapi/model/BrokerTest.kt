package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.UnauthorizedUserForAction
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.UnexpectedUserInformationException
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.USDAPI
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import ar.edu.unq.desapp.grupoc.backenddesappapi.utils.TransactionFixture
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.stream.Stream

@SpringBootTest
@Transactional
class BrokerTest {

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var quotationsService: QuotationsService

    @MockkBean
    lateinit var client: BinanceApiRestClient

    @Autowired
    lateinit var clock: Clock

    private val user = UserFixture.aUser(password = "eluber123")
    private val anotherUser = UserFixture.aUser(
        "pepito@gmail.com",
        "9506568711100060517136",
        "12345679",
        password = "eluber123"
    )
    private lateinit var broker: Broker
    private val aPrice = 1.00
    private val higherIntendedPrice = 1.05
    private val lowerIntendedPrice = 0.95
    private val cryptoSymbol = "ALICEUSDT"
    private val percentage : Double = 5.00
    val mockPrice = aPrice
    val mockSymbol = "ALICEUSDT"


    @BeforeEach
    internal fun setUp() {
        userRepository.saveAll(listOf(user, anotherUser))
        broker = Broker(percentage, transactionRepository, quotationsService, USDAPI(), clock)

        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice.toString()
        tickerPrice.symbol = mockSymbol

        every { client.getPrice(mockSymbol) } returns  tickerPrice
        every { client.getPrice("") } throws RuntimeException("Could not get the token price")
    }

    @ParameterizedTest
    @MethodSource("operationTypesAndInputs")
    fun `when a user expresses their intent, then their intent is added to their active transactions`(operationType : OperationType, walletId : String?, cvu : String?) {
        broker.expressOperationIntent(user, operationType, aPrice, cryptoSymbol, walletId, cvu, 0)

        assertThat(broker.findTransactionsOf(user).first().firstUser).usingRecursiveComparison().isEqualTo(user)
    }

    @ParameterizedTest
    @MethodSource("operationTypesAndInputs")
    fun `when two users express an intent, then they only have one active transaction each`(operationType: OperationType, walletId: String?, cvu: String?) {
        broker.expressOperationIntent(user, operationType, aPrice, cryptoSymbol, walletId, cvu, 0)
        broker.expressOperationIntent(anotherUser, operationType, aPrice, cryptoSymbol, walletId, cvu, 0)

        assertThat(broker.findTransactionsOf(anotherUser).first().firstUser).usingRecursiveComparison().isEqualTo(anotherUser)
    }

    @Test
    fun `when a user expresses a buy intent, then the wallet id is saved`() {
        val walletId = "12345678"
        broker.expressOperationIntent(user, OperationType.BUY, aPrice, cryptoSymbol, walletId = walletId, quantity = 0)

        assertThat(broker.findTransactionsOf(user).first().walletId).isEqualTo(walletId)
    }

    @Test
    fun `when a user expresses a buy intent with a cvu, then it fails and transaction is not created`() {
        assertThatThrownBy { broker.expressOperationIntent(
            user,
            OperationType.BUY,
            aPrice,
            cryptoSymbol,
            walletId = TransactionFixture.A_WALLET_ID,
            cvu = TransactionFixture.A_CVU,
            0
        ) }.isInstanceOf(UnexpectedUserInformationException::class.java)
            .hasMessage("Cannot create a BUY transaction with cvu")

        assertThat(broker.findTransactionsOf(user)).isEmpty()
    }

    @Test
    fun `when a user expresses a buy intent without a wallet id, then it fails and transaction is not created`() {
        assertThatThrownBy { broker.expressOperationIntent(
            user,
            OperationType.BUY,
            aPrice,
            cryptoSymbol,
            walletId = null,
            quantity = 0
        ) }
            .isInstanceOf(UnexpectedUserInformationException::class.java)
            .hasMessage("Cannot create a BUY transaction with walletId null")

        assertThat(broker.findTransactionsOf(user)).isEmpty()
    }

    @Test
    fun `when a user expresses a sell intent, then the cvu is saved`() {
        val cvu = "6666666666666666666666"
        broker.expressOperationIntent(user, OperationType.SELL, aPrice, cryptoSymbol, cvu = cvu, quantity = 0)

        assertThat(broker.findTransactionsOf(user).first().cvu).isEqualTo(cvu)
    }

    @Test
    fun `when a user expresses a sell intent with a walletId, then it fails and transaction is not created`() {
        assertThatThrownBy { broker.expressOperationIntent(
            user,
            OperationType.SELL,
            aPrice,
            cryptoSymbol,
            walletId = TransactionFixture.A_WALLET_ID,
            cvu = TransactionFixture.A_CVU,
            0
        ) }.isInstanceOf(UnexpectedUserInformationException::class.java)
            .hasMessage("Cannot create a SELL transaction with walletId")

        assertThat(broker.findTransactionsOf(user)).isEmpty()
    }

    @Test
    fun `when a user expresses a sell intent without a cvu, then it fails and transaction is not created`() {
        assertThatThrownBy { broker.expressOperationIntent(
            user,
            OperationType.SELL,
            aPrice,
            cryptoSymbol,
            cvu = null,
            quantity = 0
        ) }
            .isInstanceOf(UnexpectedUserInformationException::class.java)
            .hasMessage("Cannot create a SELL transaction with cvu null")

        assertThat(broker.findTransactionsOf(user)).isEmpty()
    }

    @Test
    fun `when a user tries to express a buying intent with a price 5% higher than the latest quotation then an exception is thrown`(){

        assertThatThrownBy { broker.expressOperationIntent(
            user,
            OperationType.BUY,
            higherIntendedPrice,
            cryptoSymbol,
            quantity = 0
        ) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Cannot express a transaction intent with a price 5 higher than the latest quotation")
    }

    @Test
    fun `when a user tries to express a buying intent with a price 5% lower than the latest quotation then an exception is thrown`(){

        assertThatThrownBy { broker.expressOperationIntent(
            user,
            OperationType.BUY,
            lowerIntendedPrice,
            cryptoSymbol,
            quantity = 0
        ) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Cannot express a transaction intent with a price 5 lower than the latest quotation")
    }

    @Nested
    @DisplayName("given a user who has expressed their buying intent")
    inner class BuyOperationTypeAlreadyExpressed {
        private lateinit var transaction: Transaction

        @BeforeEach
        fun setUp() {
            transaction = broker.expressOperationIntent(
                user,
                OperationType.BUY,
                aPrice,
                cryptoSymbol,
                "12345678",
                quantity = 0
            )
        }

        @Test
        fun `when the same user tries to accept the transaction, then it fails and the transaction is not processed`() {
            assertThatThrownBy { broker.processTransaction(transaction, user, aPrice, TransactionAction.ACCEPT) }
                .isInstanceOf(UnauthorizedUserForAction::class.java)
                .hasMessage("User ${user.email} is not authorized to perform action ACCEPT on transaction ${transaction.id}")

            val transaction = broker.findTransactionsOf(user).first()
            assertThat(transaction.secondUser).isNull()
            assertThat(transaction.status).isEqualTo(TransactionStatus.ACTIVE)
        }

        @Test
        fun `when another user intending to sell accepts the transaction, then that transaction is in status pending`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)

            val processedTransaction = broker.pendingTransactions().first()
            assertThat(processedTransaction.secondUser).usingRecursiveComparison().isEqualTo(anotherUser)
            assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        }

        @Test
        fun `when another user intending to sell accepts the transaction, then that transaction uses the most recent quotation`() {
            val quotation = 1.05
            broker.processTransaction(transaction, anotherUser, quotation, TransactionAction.ACCEPT)

            val processedTransaction = broker.pendingTransactions().first()
            assertThat(processedTransaction.quotation).isEqualTo(quotation)
        }

        @ParameterizedTest
        @ValueSource(doubles = [ 1.0, -1.0])
        fun `when another user intending to sell accepts the transaction but the most recent quotation is outside price band, then an exception is thrown and the operation is cancelled`(
            oppositeModifier: Double
        ) {
            val priceVariationLimit = valueHigherThanPercentage(transaction.intendedPrice, percentage)
            val quotationOutsidePriceBand = transaction.intendedPrice + (priceVariationLimit * oppositeModifier)

            assertThatThrownBy {
                broker.processTransaction(
                    transaction,
                    anotherUser,
                    quotationOutsidePriceBand,
                    TransactionAction.ACCEPT
                )
            }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Cannot process transaction, latest quotation is outside price band")

            val processedTransaction = broker.findTransactionsOf(user).first()
            assertThat(processedTransaction.status).isEqualTo(TransactionStatus.CANCELLED)
        }

        @Test
        fun `when another user accepts the transaction and informs transfer has been completed, then the transaction is waiting confirmation`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_TRANSFER)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.WAITING_CONFIRMATION)
        }

        @Test
        fun `when another user accepts the transaction and informs transfer, then the receiving user can confirm the reception`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_TRANSFER)

            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.PENDING_CRYPTO_TRANSFER)
        }

        @Test
        fun `when the transfer reception has been confirmed, then the seller can inform the crypto transfer`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)

            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.WAITING_CRYPTO_CONFIRMATION)
        }

        @Test
        fun `when the buyer confirms the crypto reception, then the transaction is completed`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)

            broker.processTransaction(transaction, user, aPrice, TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.COMPLETED)
        }

        @Test
        fun `when another user accepts the transaction but the original cancels it, then the transaction is cancelled`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)

            broker.cancelTransaction(transaction.id!!, user)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.CANCELLED)
        }

        @Test
        fun `when a transaction goes from creation to completion, then both users get reputation points`() {
            val reputationPointsBeforeTransaction = user.getReputationPoints()
            completeTransaction()

            assertThat(user.getReputationPoints()).isEqualTo(reputationPointsBeforeTransaction + 10.0)
        }

        @Test
        fun `when a transaction is cancelled, then the user who cancelled is subtracted reputation points`() {
            val userWithHighReputation = UserFixture.aUserWithReputation(50.0)
            val reputationOfCancellerBeforeTransaction = userWithHighReputation.getReputationPoints()
            val reputationOfOtherUserBeforeTransaction = anotherUser.getReputationPoints()

            broker.cancelTransaction(transaction.id!!, userWithHighReputation)

            assertThat(userWithHighReputation.getReputationPoints()).isEqualTo(reputationOfCancellerBeforeTransaction - 20.0)
            assertThat(user.getReputationPoints()).isEqualTo(reputationOfOtherUserBeforeTransaction)
        }

        private fun completeTransaction() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION)
        }
    }

    @Nested
    @DisplayName("given a user who has expressed their selling intent")
    inner class SellOperationTypeAlreadyExpressed {
        private lateinit var transaction: Transaction

        @BeforeEach
        fun setUp() {
            transaction = broker.expressOperationIntent(
                user,
                OperationType.SELL,
                aPrice,
                cryptoSymbol,
                cvu = TransactionFixture.A_CVU,
                quantity = 10
            )
        }

        @Test
        fun `when the same user tries to inform transfer has been made, then it fails and the transaction is still active`() {
            assertThatThrownBy { broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_TRANSFER) }
                .hasMessage("User ${user.email} is not authorized to perform action INFORM_TRANSFER on transaction ${transaction.id}")
                .isInstanceOf(UnauthorizedUserForAction::class.java)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.ACTIVE)
        }

        @Test
        fun `when another user informs transfer has been completed, then the transaction is waiting confirmation`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.WAITING_CONFIRMATION)
        }

        @Test
        fun `when another user informs transfer, then the receiving user can confirm the reception`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)

            broker.processTransaction(transaction, user, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.PENDING_CRYPTO_TRANSFER)
        }

        @Test
        fun `when the transfer reception has been confirmed, then the seller can inform the crypto transfer`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, user, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)

            broker.processTransaction(transaction, user, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.WAITING_CRYPTO_CONFIRMATION)
        }



    }

    private fun valueHigherThanPercentage(percentage: Double, originalValue: Double) =
        originalValue * (percentage / 100) + 0.01

    companion object {
        @JvmStatic
        fun operationTypesAndInputs(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(OperationType.BUY, "12345678", null),
                Arguments.of(OperationType.SELL, null, "4444444444444444444444")
            )
        }
    }


}