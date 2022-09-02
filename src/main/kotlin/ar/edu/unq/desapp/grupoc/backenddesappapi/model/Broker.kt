package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.util.*

class Broker {
    private val transactions: MutableList<Transaction> = mutableListOf()

    fun expressOperationIntent(user: String, operationType: OperationType): Transaction {
        val transaction = Transaction(user, TransactionStatus.ACTIVE, operationType)
        transactions.add(transaction)
        return transaction
    }

    fun findActiveTransactionsOf(user: String): List<Transaction> {
        return transactions.filter { transaction -> transaction.firstUser == user }
    }

    fun pendingTransactions(): List<Transaction> {
        return transactions.filter { transaction -> transaction.status == TransactionStatus.PENDING }
    }

    fun processTransaction(transactionId: UUID, user: String, operationType: OperationType) {
        transactions.find { transaction -> transaction.id == transactionId}!!.process(user, operationType)
    }

}
