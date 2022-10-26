package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import PriceOutsidePriceBandException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.UnexpectedUserInformationException
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
        cryptoSymbol: String,
        walletId: String? = null,
        cvu: String? = null,
        quantity: Int
    ): Transaction {
        checkQuotationWithinRange(intendedPrice, cryptoSymbol)
        validateCreationParameters(operationType, walletId, cvu)
        val transaction = Transaction(
            user,
            operationType,
            intendedPrice,
            cryptoSymbol,
            quantity,
            walletId = walletId,
            cvu = cvu
        )
        return transactionRepository.save(transaction)
    }

    private fun validateCreationParameters(operationType: OperationType, walletId: String?, cvu: String?) {
        if (operationType == OperationType.BUY && walletId == null || operationType == OperationType.SELL && cvu == null) {
            throw UnexpectedUserInformationException("Cannot create a $operationType transaction with ${if (operationType == OperationType.BUY) "walletId" else "cvu"} null")
        }
        if (operationType == OperationType.BUY && cvu != null || operationType == OperationType.SELL && walletId != null){
            throw UnexpectedUserInformationException("Cannot create a $operationType transaction with ${if (operationType == OperationType.BUY) "cvu" else "walletId"}")
        }
    }

    fun findTransactionsOf(user: BrokerUser): List<Transaction> {
        return transactionRepository.findByFirstUser(user)
    }

    fun pendingTransactions(): List<Transaction> {
        return transactionRepository.findAllByStatus(TransactionStatus.PENDING)
    }

    fun processTransaction(
        transaction: Transaction, user: BrokerUser, latestQuotation: Double, action: TransactionAction
    ): Transaction {
        val processedTransaction = action.processWith(transaction, user, latestQuotation, this)
        return transactionRepository.save(processedTransaction)
    }

    internal fun confirmCryptoTransferReception(transactionId: UUID): Transaction {
        val transaction = findTransactionById(transactionId)
        transaction.confirmCryptoTransferReception()
        scoreTracker.trackTransferReception(transaction, Instant.now())
        return transaction
    }

    internal fun confirmTransferReception(transactionId: UUID): Transaction {
        val transaction = findTransactionById(transactionId)
        transaction.confirmTransferReception()
        return transaction
    }

    internal fun validatePriceBand(transaction: Transaction, latestQuotation: Double) {
        if (priceDifferenceIsHigherThan(percentage, transaction.intendedPrice, latestQuotation)) {
            transaction.cancel()
            throw RuntimeException("Cannot process transaction, latest quotation is outside price band")
        }
    }

    internal fun informCryptoTransfer(transactionId: UUID): Transaction {
        return findTransactionById(transactionId).informCryptoTransfer()
    }

    internal fun informTransfer(transaction: Transaction, user: BrokerUser): Transaction {
        if (transaction.operationType == OperationType.SELL) {
            transaction.secondUser = user
        }
        return transaction.informTransfer()
    }

    fun cancelTransaction(transactionId: UUID, cancellingUser: BrokerUser) {
        findTransactionById(transactionId).cancel()
        scoreTracker.trackTransactionCancellation(cancellingUser)
    }

    private fun checkQuotationWithinRange(intendedPrice: Double, cryptoSymbol: String) {
        val tickerPrice = quotationsService.getTokenPrice(cryptoSymbol).price.toDouble()
        if (priceDifferenceIsHigherThan(percentage, intendedPrice, tickerPrice) && intendedPrice > tickerPrice) {
            throw PriceOutsidePriceBandException("Cannot express a transaction intent with a price 5 higher than the latest quotation")
        }
        if (priceDifferenceIsHigherThan(percentage, intendedPrice, tickerPrice) && intendedPrice < tickerPrice) {
            throw PriceOutsidePriceBandException("Cannot express a transaction intent with a price 5 lower than the latest quotation")
        }
    }

    private fun priceDifferenceIsHigherThan(percentage: Double, intendedPrice: Double, latestPrice: Double): Boolean {
        val priceDifference = intendedPrice - latestPrice
        val percentageDifference = (kotlin.math.abs(priceDifference) / latestPrice) * 100
        return percentageDifference > percentage
    }

    private fun findTransactionById(transactionId: UUID) = transactionRepository.findById(transactionId).get()

    fun activeTransactions(): List<Transaction> {
        return transactionRepository.findAllByStatus(TransactionStatus.ACTIVE)
    }

}
