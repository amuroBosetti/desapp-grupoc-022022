package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

class ActiveTransactionDTO(
    @JsonProperty("ownerId")
    val ownerId: Long,
    @JsonProperty("intendedPrice")
    val intendedPrice: Double,
    @JsonProperty("createdAt")
    val createdAt: Instant,
    @JsonProperty("symbol")
    val symbol: String,
    @JsonProperty("transactionId")
    val transactionId: UUID
) {

    companion object {
        fun from(transaction: Transaction): ActiveTransactionDTO {
            return ActiveTransactionDTO(
                transaction.firstUser.id!!,
                transaction.intendedPrice,
                transaction.createadAt,
                transaction.symbol,
                transaction.id!!
            )
        }
    }


}
