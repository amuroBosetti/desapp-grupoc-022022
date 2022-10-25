package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.OperationType
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import java.util.*

data class TransactionCreationResponseDTO(
    val operationId: UUID,
    val symbol: String,
    val intendedPrice: Double,
    val operationType: OperationType,
    val quantity: Int,
    val walletId: String? = null,
    val cvu: String? = null
) {

    companion object {
        fun from(transaction: Transaction): TransactionCreationResponseDTO {
            return TransactionCreationResponseDTO(
                transaction.id!!,
                transaction.symbol,
                transaction.intendedPrice,
                transaction.operationType,
                transaction.quantity,
                transaction.walletId,
                transaction.cvu
            )
        }
    }
}
