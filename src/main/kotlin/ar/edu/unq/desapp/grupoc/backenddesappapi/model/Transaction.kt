package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.util.UUID

class Transaction(
    val firstUser: String,
    var status: TransactionStatus,
    val operationType: OperationType,
    val intendedPrice: Double
) {

    var secondUser: String? = null
    var quotation: Double? = null
    val id: UUID = UUID.randomUUID()

    fun process(secondUser: String, secondUserIntent: OperationType, latestQuotation: Double) {
        validateCompatibleIntents(secondUserIntent)
        status = TransactionStatus.PENDING
        this.secondUser = secondUser
        this.quotation = latestQuotation
    }

    private fun validateCompatibleIntents(secondUserIntent: OperationType) {
        if (secondUserIntent == operationType) {
            throw RuntimeException("Cannot process a transaction where both user intents is $operationType")
        }
    }

    fun isPending(): Boolean {
        return this.status == TransactionStatus.PENDING
    }

    fun informTransfer() {
        this.status = TransactionStatus.WAITING_CONFIRMATION
    }

    fun confirmReception() {
        this.status = TransactionStatus.COMPLETED
    }

    fun cancel() {
        this.status = TransactionStatus.CANCELLED
    }

}
