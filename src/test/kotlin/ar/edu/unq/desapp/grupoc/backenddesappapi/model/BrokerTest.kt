package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BrokerTest {

    private lateinit var broker: Broker

    @BeforeEach
    internal fun setUp() {
        broker = Broker()
    }

    @Test
    fun `when a user expresses their buy intent, then their intent is added to their active transactions`() {
        val user = "pepito@gmail.com"

        broker.expressOperationIntent(user, OperationType.BUY)

        assertThat(broker.findActiveTransactionsOf(user).first().firstUser).isEqualTo(user)
    }

    @Test
    fun `when two users express their buy intent, then they only have one active transaction each`() {
        val user = "pepito@gmail.com"
        val anotherUser = "fulanito@gmail.com"

        broker.expressOperationIntent(user, OperationType.BUY)
        broker.expressOperationIntent(anotherUser, OperationType.BUY)

        assertThat(broker.findActiveTransactionsOf(anotherUser).first().firstUser).isEqualTo(anotherUser)
    }

    @Nested
    @DisplayName("given a user who has expressed their buy intent")
    inner class BuyOperationTypeAlreadyExpressed {
        private lateinit var transaction: Transaction
        private val user = "pepito@gmail.com"

        @BeforeEach
        fun setUp() {
            transaction = broker.expressOperationIntent(user, OperationType.BUY)
        }

        @Test
        fun `when another user intending to sell accepts the transaction, then that transaction is in status pending`() {
            val anotherUser = "fulanito@gmail.com"

            broker.processTransaction(transaction.id, anotherUser, OperationType.SELL)

            val processedTransaction = broker.pendingTransactions().first()
            assertThat(processedTransaction.secondUser).isEqualTo(anotherUser)
            assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        }

        @Test
        fun `another user cannot accept that transaction if they also intend to buy, and the transaction is not proccesed`() {
            val anotherUser = "fulanito@gmail.com"

            assertThatThrownBy { broker.processTransaction(transaction.id, anotherUser, OperationType.BUY) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Cannot process a transaction where both users intents is BUY")

            val transaction = broker.findActiveTransactionsOf(user).first()
            assertThat(transaction.secondUser).isNull()
            assertThat(transaction.status).isEqualTo(TransactionStatus.ACTIVE)
        }

    }



}