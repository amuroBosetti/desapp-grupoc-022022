package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BrokerTest {

    private val broker: Broker = Broker()

    @Test
    fun `cuando un usuario expresa su intencion de compra, luego esta intencion se encuentra entre las transacciones activas del usuario`() {
        val user = "pepito@gmail.com"

        broker.expressOperationIntent(user)

        assertThat(broker.findActiveTransactionsOf(user).first().firstUser).isEqualTo(user)
    }

    @Test
    fun `cuando dos usuarios expresan su intencion de compra, luego solo una transaccion activa pertenece a un usuario`() {
        val user = "pepito@gmail.com"
        val anotherUser = "fulanito@gmail.com"

        broker.expressOperationIntent(user)
        broker.expressOperationIntent(anotherUser)

        assertThat(broker.findActiveTransactionsOf(anotherUser).first().firstUser).isEqualTo(anotherUser)
    }

    @Nested
    @DisplayName("dado un usuario que ha expresado su intencion de compra")
    inner class BuyIntentAlreadyExpressed {
        private lateinit var transaction: Transaction
        private val user = "pepito@gmail.com"

        @BeforeEach
        fun setUp() {
            transaction = broker.expressOperationIntent(user)
        }

        @Test
        fun `cuando un usuario buscando vender acepta esa transaccion, luego la transaccion se encuentra en estado pendiente`() {
            val anotherUser = "fulanito@gmail.com"

            broker.processTransaction(transaction.id, anotherUser)

            val processedTransaction = broker.pendingTransactions().first()
            assertThat(processedTransaction.secondUser).isEqualTo(anotherUser)
            assertThat(processedTransaction.status).isEqualTo(TransactionStatus.PENDING)
        }

        @Disabled
        fun `cuando un usuario buscando comprar acepta esa transaccion, se levanta una excepcion y la transaccion no se procesa`() {
            //TODO
            val anotherUser = "fulanito@gmail.com"

            assertThatThrownBy { broker.processTransaction(transaction.id, anotherUser) }
                .isInstanceOf(RuntimeException::class.java)
                .hasMessage("Cannot process a transaction where both users want to buy")

            val processedTransaction = broker.pendingTransactions().first()
            assertThat(processedTransaction.secondUser).isNull()
            assertThat(processedTransaction.status).isEqualTo(TransactionStatus.ACTIVE)
        }

    }



}