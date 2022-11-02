package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
class Transaction(
    @ManyToOne
    val firstUser: BrokerUser,
    val operationType: OperationType,
    //Might be -+ 5 % different than its quotation at that moment, it's what was paid
    val intendedPrice: Double,
    val symbol: String,
    val quantity: Int,
    var walletId: String? = null,
    var cvu: String? = null,
    // USD to ARS official coversion rate
    var usdToArs: Double? = null,
    // This is nominals quantity * price in USD
    var amountInUSD: Double? = 0.00,
    // This is USD * USD to ARS official conversion rate
    var amountInARS: Double? = 0.00
) {
    @CreationTimestamp
    val createadAt = Instant.now() //TODO: esto deberia crearlo la base de datos

    @ManyToOne
    var secondUser: BrokerUser? = null
    var quotation: Double? = null
    var status: TransactionStatus = TransactionStatus.ACTIVE
    var completionDate: LocalDate? = null

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null

    fun accept(secondUser: BrokerUser, latestQuotation: Double): Transaction {
        status = status.accept()
        if (this.secondUser == null) this.secondUser = secondUser
        if (this.quotation == null) this.quotation = latestQuotation
        return this
    }

    fun isPending(): Boolean {
        return this.status == TransactionStatus.PENDING
    }

    fun informTransfer(): Transaction {
        this.status = this.status.informTransfer(this)
        return this
    }

    fun confirmReception() {
        this.status = this.status.confirmReception()
    }

    fun cancel() {
        this.status = TransactionStatus.CANCELLED
    }

    fun confirmTransferReception() {
        this.status = this.status.confirmTransferReception()
    }

    fun confirmCryptoTransferReception() {
        this.status = this.status.confirmCryptoTransferReception()
    }

    fun informCryptoTransfer(): Transaction {
        this.status = this.status.informCryptoTransfer()
        return this
    }

}