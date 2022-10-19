package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionWithSameUserInBothSidesException
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.HashMap

@SpringBootTest
@Transactional
class BrokerTest {

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private val user = UserFixture.aUser()
    private val anotherUser = UserFixture.aUser("pepito@gmail.com", "9506568711100060517136", "12345679")
    private lateinit var broker: Broker
    private val aPrice = 1.00
    private val higherIntendedPrice = 1.05
    private val lowerIntendedPrice = 0.95
    private val cryptoSymbol = "ALICEUSDT"
    private val quotations = HashMap<String, Double>()
    private val percentage : Double = 5.00

    @BeforeEach
    internal fun setUp() {
        userRepository.saveAll(listOf(user, anotherUser))
        quotations.put("ALICEUSDT",aPrice)
        broker = Broker(quotations, percentage, transactionRepository)
    }

    @ParameterizedTest
    @EnumSource(OperationType::class)
    fun `when a user expresses their intent, then their intent is added to their active transactions`(operationType : OperationType) {
        broker.expressOperationIntent(user, operationType, aPrice, cryptoSymbol)

        assertThat(broker.findTransactionsOf(user).first().firstUser).usingRecursiveComparison().isEqualTo(user)
    }

    @ParameterizedTest
    @EnumSource(OperationType::class)
    fun `when two users express an intent, then they only have one active transaction each`(operationType: OperationType) {
        broker.expressOperationIntent(user, operationType, aPrice, cryptoSymbol)
        broker.expressOperationIntent(anotherUser, operationType, aPrice, cryptoSymbol)

        assertThat(broker.findTransactionsOf(anotherUser).first().firstUser).usingRecursiveComparison().isEqualTo(anotherUser)
    }

    @Test
    fun `when a user tries to express a buying intent with a price 5% higher than the latest quotation then an exception is thrown`(){

        assertThatThrownBy { broker.expressOperationIntent(user, OperationType.BUY, higherIntendedPrice, cryptoSymbol) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Cannot express a transaction intent with a price 5 higher than the latest quotation")
    }

    @Test
    fun `when a user tries to express a buying intent with a price 5% lower than the latest quotation then an exception is thrown`(){

        assertThatThrownBy { broker.expressOperationIntent(user, OperationType.BUY, lowerIntendedPrice, cryptoSymbol) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Cannot express a transaction intent with a price 5 lower than the latest quotation")
    }

    @Nested
    @DisplayName("given a user who has expressed their buying intent")
    inner class BuyOperationTypeAlreadyExpressed {
        private lateinit var transaction: Transaction

        @BeforeEach
        fun setUp() {
            transaction = broker.expressOperationIntent(user, OperationType.BUY, aPrice, cryptoSymbol)
        }

        @Test
        fun `when the same user tries to accept the transaction, then it fails and the transaction is not processed`() {
            assertThatThrownBy { broker.processTransaction(transaction, user, aPrice, TransactionAction.ACCEPT) }
                .isInstanceOf(TransactionWithSameUserInBothSidesException::class.java)
                .hasMessage("Cannot process transaction with id ${transaction.id!!} because both sides are the same user")

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

            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.WAITING_CONFIRMATION)
        }

        @Test
        fun `when another user accepts the transaction and informs transfer, then the receiving user can confirm the reception`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)

            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.PENDING_CRYPTO_TRANSFER)
        }

        @Test
        fun `when the transfer reception has been confirmed, then the seller can inform the crypto transfer`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)

            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)

            val informedTransaction = broker.findTransactionsOf(user).first()
            assertThat(informedTransaction.status).isEqualTo(TransactionStatus.WAITING_CRYPTO_CONFIRMATION)
        }

        @Test
        fun `when the buyer confirms the crypto reception, then the transaction is completed`() {
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.ACCEPT)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)

            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION)

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
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_TRANSFER_RECEPTION)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.INFORM_CRYPTO_TRANSFER)
            broker.processTransaction(transaction, anotherUser, aPrice, TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION)
        }
    }

    private fun valueHigherThanPercentage(percentage: Double, originalValue: Double) =
        originalValue * (percentage / 100) + 0.01


}