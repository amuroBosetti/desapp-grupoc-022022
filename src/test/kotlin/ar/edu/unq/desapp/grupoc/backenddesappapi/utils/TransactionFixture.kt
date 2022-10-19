package ar.edu.unq.desapp.grupoc.backenddesappapi.utils

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import java.util.*

class TransactionFixture {
    companion object {
        fun aTransaction(userEmail: String, status: TransactionStatus = TransactionStatus.ACTIVE): Transaction {
            val transaction = Transaction(UserFixture.aUser(userEmail, userId = 5L), OperationType.BUY, 10.0, "ALICEUSDT")
            transaction.id = UUID.randomUUID()
            transaction.status = status
            return transaction
        }
    }

}
