package ar.edu.unq.desapp.grupoc.backenddesappapi.utils

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.UserFixture
import java.util.*

class TransactionFixture {
    companion object {
        const val A_WALLET_ID: String = "12345678"
        const val A_CVU: String = "5555555555555555555555"

        fun aTransaction(userEmail: String, status: TransactionStatus = TransactionStatus.ACTIVE): Transaction {
            val transaction = Transaction(
                UserFixture.aUser(userEmail, userId = 5L, password = "eluber123"),
                OperationType.BUY,
                10.0,
                "ALICEUSDT",
                quantity = 0
            )
            transaction.id = UUID.randomUUID()
            transaction.status = status
            return transaction
        }
    }

}
