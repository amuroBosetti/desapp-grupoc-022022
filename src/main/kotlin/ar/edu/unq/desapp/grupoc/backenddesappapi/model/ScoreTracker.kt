package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class ScoreTracker {
    fun trackTransferReception(transaction: Transaction) {
        transaction.firstUser.increaseReputationPoints(10.0)
        transaction.secondUser!!.increaseReputationPoints(10.0)
    }

}
