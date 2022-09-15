package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScoreTrackerTest {

    private val aUserWithHighReputation: User = UserFixture.aUserWithReputation(50.0)
    private val aUserWithLowReputation: User = UserFixture.aUserWithReputation(10.0)
    private val scoreTracker: ScoreTracker = ScoreTracker()

    @Test
    fun `when an operation is cancelled, then the cancelling user is subtracted 20 reputation points`() {
        val previousReputationScore = aUserWithHighReputation.getReputationPoints()

        scoreTracker.trackTransactionCancellation(aUserWithHighReputation)

        assertThat(aUserWithHighReputation.getReputationPoints()).isEqualTo(previousReputationScore - 20.0)
    }

    @Test
    fun `when an operation is cancelled but the cancelling user has less than 20 reputation points, then the user reputation is 0`() {
        val previousReputationScore = aUserWithLowReputation.getReputationPoints()

        scoreTracker.trackTransactionCancellation(aUserWithLowReputation)

        assertThat(previousReputationScore).isGreaterThan(0.0).isLessThan(20.0)
        assertThat(aUserWithLowReputation.getReputationPoints()).isEqualByComparingTo(0.0)
    }

    @Test
    fun `when an operation goes from creation to completion in less than 30 minutes, then both users get 10 points added`() {
        val user1PreviousScore = aUserWithLowReputation.getReputationPoints()
        val user2PreviousScore = aUserWithHighReputation.getReputationPoints()
        val transaction = getTransaction()

        scoreTracker.trackTransferReception(transaction, transaction.createadAt)

        assertThat(aUserWithLowReputation.getReputationPoints()).isEqualByComparingTo(user1PreviousScore + 10.0)
        assertThat(aUserWithHighReputation.getReputationPoints()).isEqualByComparingTo(user2PreviousScore + 10.0)
    }

    @Test
    fun `when an operation goes from creation to completion in more than 30 minutes, then both users get 5 points added`() {
        val user1PreviousScore = aUserWithLowReputation.getReputationPoints()
        val user2PreviousScore = aUserWithHighReputation.getReputationPoints()
        val transaction = getTransaction()

        scoreTracker.trackTransferReception(transaction, transaction.createadAt.plusMinutes(31))

        assertThat(aUserWithLowReputation.getReputationPoints()).isEqualByComparingTo(user1PreviousScore + 5.0)
        assertThat(aUserWithHighReputation.getReputationPoints()).isEqualByComparingTo(user2PreviousScore + 5.0)
    }

    private fun getTransaction(): Transaction {
        val transaction = Transaction(aUserWithLowReputation, OperationType.BUY, 1.01)
        transaction.accept(aUserWithHighReputation, OperationType.SELL, 1.01)
        return transaction
    }

}