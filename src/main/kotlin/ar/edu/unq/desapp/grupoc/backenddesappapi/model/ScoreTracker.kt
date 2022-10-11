package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.time.Instant

class ScoreTracker {
    fun trackTransferReception(transaction: Transaction, now: Instant) {
        val amountToAdd = if (transaction.createadAt.plusSeconds(1800).isBefore(now)) 5.0 else 10.0
        transaction.firstUser.increaseReputationPoints(amountToAdd)
        transaction.secondUser!!.increaseReputationPoints(amountToAdd)
    }

    fun trackTransactionCancellation(cancellingUser: BrokerUser) {
        cancellingUser.subtractReputationPoints(20.0)
    }

}
