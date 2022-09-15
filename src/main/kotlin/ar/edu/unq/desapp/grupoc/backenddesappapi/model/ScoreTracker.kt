package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.time.LocalDateTime

class ScoreTracker {
    fun trackTransferReception(transaction: Transaction, now: LocalDateTime) {
        val amountToAdd = if (transaction.createadAt.plusMinutes(30).isBefore(now)) 5.0 else 10.0
        transaction.firstUser.increaseReputationPoints(amountToAdd)
        transaction.secondUser!!.increaseReputationPoints(amountToAdd)
    }

    fun trackTransactionCancellation(cancellingUser: User) {
        cancellingUser.subtractReputationPoints(20.0)
    }

}
