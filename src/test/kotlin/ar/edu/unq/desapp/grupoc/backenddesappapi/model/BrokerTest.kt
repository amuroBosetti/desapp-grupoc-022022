package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class BrokerTest {

    private val user = "pepito@gmail.com"
    private val anotherUser = "fulanito@gmail.com"
    private lateinit var broker: Broker

    @BeforeEach
    internal fun setUp() {
        broker = Broker()
    }

    @ParameterizedTest
    @EnumSource(OperationType::class)
    fun `when a user expresses their intent, then their intent is added to their active transactions`(operationType : OperationType) {
        broker.expressOperationIntent(user, operationType)

        assertThat(broker.findActiveTransactionsOf(user).first().firstUser).isEqualTo(user)
    }

    @Test
    fun `when two users express their intent, then they only have one active transaction each`() {
        broker.expressOperationIntent(user, OperationType.BUY)
        broker.expressOperationIntent(anotherUser, OperationType.BUY)

        assertThat(broker.findActiveTransactionsOf(anotherUser).first().firstUser).isEqualTo(anotherUser)
    }

    @ParameterizedTest
    @EnumSource(OperationType::class)
    fun `a user cannot express an intent if the price is outside the latest quotation price band`(operationType : OperationType) {
//        broker.expressOperationIntent(user, operationType, parametro de cotizacion, parametro de codigo de crypto?)
//
//        assertThat(broker.findActiveTransactionsOf(user).first().firstUser).isEqualTo(user)
        //TODO
    }

    @Nested
    @DisplayName("given a user who has expressed their buy intent")
    inner class BuyOperationTypeAlreadyExpressed {
        private lateinit var transaction: Transaction

        @BeforeEach
        fun setUp() {
            transaction = broker.expressOperationIntent(user, OperationType.BUY)
        }

        @Test
        fun `when another user intending to sell accepts the transaction, then that transaction is in status pending`() {
            broker.processTransaction(transaction.id, anotherUser, OperationType.SELL)

            val processedTransaction = broker.pendingTransactions().first()
            assertThat(processedTransaction.secondUser).isEqualTo(anotherUser)
            assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        }

        @Test
        fun `when another user accepts the transaction but they also intend to buy, an exception is thrown and the transaction is not proccesed`() {
            assertThatThrownBy { broker.processTransaction(transaction.id, anotherUser, OperationType.BUY) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Cannot process a transaction where both user intents is BUY")

            val transaction = broker.findActiveTransactionsOf(user).first()
            assertThat(transaction.secondUser).isNull()
            assertThat(transaction.status).isEqualTo(TransactionStatus.ACTIVE)
        }

    }



}