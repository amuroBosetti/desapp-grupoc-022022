package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.util.UUID

class Transaction(val firstUser: String, var status: TransactionStatus) {

    lateinit var secondUser: String
    val id: UUID = UUID.randomUUID()

    fun process(user: String) {
        status = TransactionStatus.PENDING
        secondUser = user
    }

}
