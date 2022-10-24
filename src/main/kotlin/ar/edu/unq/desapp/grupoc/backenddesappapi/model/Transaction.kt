package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
class Transaction(
    @ManyToOne
    val firstUser: BrokerUser,
    val operationType: OperationType,
    val intendedPrice: Double,
    val symbol: String
) {

    val createadAt: Instant = Instant.now() //TODO esto deberia crearlo la base de datos
    @ManyToOne
    var secondUser: BrokerUser? = null
    var quotation: Double? = null
    var status: TransactionStatus = TransactionStatus.ACTIVE

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null

    fun accept(secondUser: BrokerUser, latestQuotation: Double) : Transaction {
        status = status.accept()
        this.secondUser = secondUser
        this.quotation = latestQuotation
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