package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.util.*


class Broker {
    private val transactions: MutableList<Transaction> = mutableListOf()
    private val percentage = 5

    fun expressOperationIntent(user: String, operationType: OperationType, intendedPrice: Double, cryptoSymbol: String): Transaction {
        checkQuotationWithinRange(intendedPrice, cryptoSymbol)
        val transaction = Transaction(user, TransactionStatus.ACTIVE, operationType)
        transactions.add(transaction)
        return transaction
    }

    fun checkQuotationWithinRange(intendedPrice: Double, cryptoSymbol: String) {
        val latestPrice = latestQuotation(cryptoSymbol)!!
        if (priceDifferenceIsHigherThan(percentage, intendedPrice, latestPrice) && intendedPrice > latestPrice){
            throw RuntimeException("Cannot express a transaction intent with a price 5 higher than the latest quotation")
        }
        if(priceDifferenceIsHigherThan(percentage, intendedPrice, latestPrice) && intendedPrice < latestPrice){
            throw RuntimeException("Cannot express a transaction intent with a price 5 lower than the latest quotation")
        }
    }

    private fun latestQuotation(cryptoSymbol: String): Double? {
        val quotations : HashMap<String, Double> = HashMap<String, Double>()
        quotations.put("ALICEUSDT",1.01)
        return quotations[cryptoSymbol]
    }

    private fun priceDifferenceIsHigherThan(percentage: Int, intendedPrice: Double, latestPrice: Double): Boolean {
        val priceDifference = intendedPrice - latestPrice
        val percentageDifference = (priceDifference / latestPrice) * 100
        return percentageDifference > percentage
    }

    fun findActiveTransactionsOf(user: String): List<Transaction> {
        return transactions.filter { transaction -> transaction.firstUser == user }
    }

    fun pendingTransactions(): List<Transaction> {
        return transactions.filter { transaction -> transaction.isPending() }
    }

    fun processTransaction(transactionId: UUID, user: String, operationType: OperationType) {
        transactions.find { transaction -> transaction.id == transactionId}!!.process(user, operationType)
    }

}
