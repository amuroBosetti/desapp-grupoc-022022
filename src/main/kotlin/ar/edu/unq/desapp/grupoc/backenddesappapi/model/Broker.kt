package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import java.time.Instant
import java.util.*


class Broker(
    var percentage: Double,
    private val transactionRepository: TransactionRepository,
    val quotationsService: QuotationsService
) {
    private val scoreTracker: ScoreTracker = ScoreTracker()

    fun expressOperationIntent(
        user: BrokerUser,
        operationType: OperationType,
        intendedPrice: Double,
        cryptoSymbol: String
    ): Transaction {
        checkQuotationWithinRange(intendedPrice, cryptoSymbol)
        val transaction = Transaction(user, operationType, intendedPrice, cryptoSymbol)
        return transactionRepository.save(transaction)
    }

    fun findTransactionsOf(user: BrokerUser): List<Transaction> {
        return transactionRepository.findByFirstUser(user)
    }

    fun pendingTransactions(): List<Transaction> {
        return transactionRepository.findAllByStatus(TransactionStatus.PENDING)
    }

    fun processTransaction(
        transactionId: UUID,
        acceptingUser: BrokerUser,
        operationType: OperationType,
        latestQuotation: Double
    ) {
        val transaction = findTransactionById(transactionId)
        if (priceDifferenceIsHigherThan(percentage, transaction.intendedPrice, latestQuotation)) {
            transaction.cancel()
            throw RuntimeException("Cannot process transaction, latest quotation is outside price band")
        } else {
            transaction.accept(acceptingUser, operationType, latestQuotation)
        }
    }

    fun informTransfer(transactionId: UUID) {
        findTransactionById(transactionId).informTransfer()
    }

    fun confirmReception(transactionId: UUID, now: Instant) {
        val transaction = findTransactionById(transactionId)
        transaction.confirmReception()
        scoreTracker.trackTransferReception(transaction, now)
    }

    fun cancelTransaction(transactionId: UUID, cancellingUser: BrokerUser) {
        findTransactionById(transactionId).cancel()
        scoreTracker.trackTransactionCancellation(cancellingUser)
    }

    private fun checkQuotationWithinRange(intendedPrice: Double, cryptoSymbol: String) {
        val tickerPrice = quotationsService.getTokenPrice(cryptoSymbol).price.toDouble()
        if (priceDifferenceIsHigherThan(percentage, intendedPrice, tickerPrice) && intendedPrice > tickerPrice) {
            throw RuntimeException("Cannot express a transaction intent with a price 5 higher than the latest quotation")
        }
        if (priceDifferenceIsHigherThan(percentage, intendedPrice, tickerPrice) && intendedPrice < tickerPrice) {
            throw RuntimeException("Cannot express a transaction intent with a price 5 lower than the latest quotation")
        }
    }

    private fun priceDifferenceIsHigherThan(percentage: Double, intendedPrice: Double, latestPrice: Double): Boolean {
        val priceDifference = intendedPrice - latestPrice
        val percentageDifference = (kotlin.math.abs(priceDifference) / latestPrice) * 100
        return percentageDifference > percentage
    }

    private fun findTransactionById(transactionId: UUID) =
        transactionRepository.findById(transactionId).get()

    fun activeTransactions(): List<Transaction> {
        return transactionRepository.findAllByStatus(TransactionStatus.ACTIVE)
    }

}
