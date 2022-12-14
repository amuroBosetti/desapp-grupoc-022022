package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.InvalidTransactionStatusException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class TransactionTest {

    private val user: BrokerUser = UserFixture.aUser(
        cvu = "9506368711100060517136",
        walletId = "12345678",
        userId = 5L,
        password = "eluber123"
    )
    private val acceptingUser: BrokerUser = UserFixture.aUser(
        "pepito@gmail.com",
        "9506368711100060517136",
        "12345678",
        5L,
        "eluber123"
    )
    private val operationType : OperationType = OperationType.BUY
    private val intendedPrice : Double = 1.01

    @Test
    fun `when a transaction is created, then it's status is active and the intending user and price are saved`() {
        val transaction : Transaction = getActiveTransaction()

        assertThat(transaction.status).isEqualTo(TransactionStatus.ACTIVE)
        assertThat(transaction.firstUser).isEqualTo(user)
        assertThat(transaction.intendedPrice).isEqualTo(intendedPrice)
    }

    @Test
    fun `when an active transaction is accepted by another user, then it's status is pending and the accepting user and quotation are saved`() {
        val transaction : Transaction = getActiveTransaction()

        transaction.accept(acceptingUser, intendedPrice)

        assertThat(transaction.status).isEqualTo(TransactionStatus.PENDING)
        assertThat(transaction.secondUser).isEqualTo(acceptingUser)
        assertThat(transaction.quotation).isEqualTo(intendedPrice)
    }

    @Test
    fun `when a user informs the transfer for a pending transaction, then it's status is waiting confirmation`() {
        val transaction = getPendingTransaction()

        transaction.informTransfer()

        assertThat(transaction.status).isEqualTo(TransactionStatus.WAITING_CONFIRMATION)
    }

    @Test
    fun `when a user confirms the transfer reception for a waiting confirmation transaction, then it is completed`() {
        val transaction = getWaitingConfirmationTransaction()

        transaction.confirmReception()

        assertThat(transaction.status).isEqualTo(TransactionStatus.COMPLETED)
    }

    @Test
    fun `when a user confirms the transfer reception for a non-waiting confirmation transaction, then an exception is thrown`() {
        val transaction = getPendingTransaction()

        assertThatThrownBy { transaction.confirmReception() }
            .isInstanceOf(InvalidTransactionStatusException::class.java)
            .hasMessage("Invalid status PENDING for action ${TransactionAction.CONFIRM_TRANSFER_RECEPTION}")

        assertThat(transaction.status).isEqualTo(TransactionStatus.PENDING)
    }

    @Test
    fun `when a user informs the transfer for a non-pending transaction, then an exception is thrown`() {
        val transaction = getActiveTransaction()

        assertThatThrownBy { transaction.informTransfer() }
            .isInstanceOf(InvalidTransactionStatusException::class.java)
            .hasMessage("Invalid status ACTIVE for action ${TransactionAction.INFORM_TRANSFER}")

        assertThat(transaction.status).isEqualTo(TransactionStatus.ACTIVE)
    }

    @Test
    fun `when a user accepts a non active transaction, then an exception is thrown`() {
        val transaction = getPendingTransaction()

        assertThatThrownBy { transaction.accept(acceptingUser, intendedPrice) }
            .isInstanceOf(InvalidTransactionStatusException::class.java)
            .hasMessage("Invalid status PENDING for action ${TransactionAction.ACCEPT}")

        assertThat(transaction.status).isEqualTo(TransactionStatus.PENDING)
    }

    @Test
    fun `when a user tries to do anything with a cancelled transaction, then an exception is thrown`() {
        val transaction = getCancelledTransaction()

        assertThatThrownBy { transaction.accept(acceptingUser, intendedPrice) }
            .isInstanceOf(InvalidTransactionStatusException::class.java)
            .hasMessage("Invalid status CANCELLED for action ${TransactionAction.ACCEPT}")

        assertThat(transaction.status).isEqualTo(TransactionStatus.CANCELLED)
    }

    @Test
    fun `when a user tries to do anything with a completed transaction, then an exception is thrown`() {
        val transaction = getCompletedTransaction()

        assertThatThrownBy { transaction.accept(acceptingUser, intendedPrice) }
            .isInstanceOf(InvalidTransactionStatusException::class.java)
            .hasMessage("Invalid status COMPLETED for action ${TransactionAction.ACCEPT}")

        assertThat(transaction.status).isEqualTo(TransactionStatus.COMPLETED)
    }

    private fun getCompletedTransaction(): Transaction {
        val transaction = getWaitingConfirmationTransaction()
        transaction.confirmReception()
        return transaction
    }

    private fun getCancelledTransaction(): Transaction {
        val transaction = getPendingTransaction()
        transaction.cancel()
        return transaction
    }

    private fun getWaitingConfirmationTransaction(): Transaction {
        val transaction = getPendingTransaction()
        transaction.informTransfer()
        return transaction
    }

    private fun getPendingTransaction(): Transaction {
        val transaction = getActiveTransaction()
        transaction.accept(acceptingUser, intendedPrice)
        return transaction
    }

    private fun getActiveTransaction() = Transaction(user, operationType, intendedPrice, "BNBUSDT", quantity = 0)
}