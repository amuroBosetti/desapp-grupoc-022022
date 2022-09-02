package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.util.UUID

class Transaction(val firstUser: String, var status: TransactionStatus, val operationType: OperationType) {

    var secondUser: String? = null
    val id: UUID = UUID.randomUUID()

    fun process(secondUser: String, secondUserIntent: OperationType) {
        validateCompatibleIntents(secondUserIntent)
        status = TransactionStatus.PENDING
        this.secondUser = secondUser
    }

    private fun validateCompatibleIntents(secondUserIntent: OperationType) {
        if (secondUserIntent == operationType) {
            throw RuntimeException("Cannot process a transaction where both user intents is $operationType")
        }
    }

}
