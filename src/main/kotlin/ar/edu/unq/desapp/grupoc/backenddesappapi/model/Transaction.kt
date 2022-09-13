package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.util.UUID

class Transaction(
    val firstUser: String,
    val operationType: OperationType,
    val intendedPrice: Double
) {

    var secondUser: String? = null
    var quotation: Double? = null
    var status: TransactionStatus = TransactionStatus.ACTIVE
    val id: UUID = UUID.randomUUID()

    fun accept(secondUser: String, secondUserIntent: OperationType, latestQuotation: Double) {
        validateCompatibleIntents(secondUserIntent)
        status = status.accept()
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
        this.status = this.status.informTransfer()
    }

    fun confirmReception() {
        this.status = this.status.confirmReception()
    }

    fun cancel() {
        this.status = TransactionStatus.CANCELLED
    }

}
