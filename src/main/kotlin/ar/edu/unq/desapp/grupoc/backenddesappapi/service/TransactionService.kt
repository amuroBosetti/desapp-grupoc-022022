package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionNotFoundException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.*
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TradedVolumeResponseDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationResponseDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*
import javax.annotation.PostConstruct

@Service
class TransactionService {

    private lateinit var broker: Broker

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var quotationsService: QuotationsService

    @PostConstruct
    fun init() {
        broker = Broker(5.0, transactionRepository, quotationsService, DollarAPI())
    }

    fun createTransaction(userEmail: String, transactionCreationDTO: TransactionCreationDTO):
            TransactionCreationResponseDTO {
        val user = findUser(userEmail)
        val savedTransaction = broker.expressOperationIntent(
            user,
            transactionCreationDTO.operationType,
            transactionCreationDTO.intendedPrice,
            transactionCreationDTO.symbol,
            transactionCreationDTO.walletId,
            transactionCreationDTO.cvu,
            transactionCreationDTO.quantity
        )
        return TransactionCreationResponseDTO.from(savedTransaction)
    }

    private fun findUser(userEmail: String): BrokerUser {
        if (userEmail.isBlank()) {
            throw RuntimeException("User email cannot be blank")
        }
        return userRepository.findByEmail(userEmail)
            ?: throw NotRegisteredUserException(userEmail)
    }

    fun getActiveTransactions(): List<Transaction> {
        return broker.activeTransactions()
    }

    fun processTransaction(transactionId: UUID, userEmail: String, action: TransactionAction): Transaction {
        val user = userRepository.findByEmail(userEmail) ?: throw NotRegisteredUserException(userEmail)
        val transaction = transactionRepository.findById(transactionId).orElseThrow {
            TransactionNotFoundException(transactionId)
        }
        val latestQuotation = quotationsService.getTokenPrice(transaction.symbol).price.toDouble()
        return broker.processTransaction(transaction, user, latestQuotation, action)
    }

    fun getTradedVolume(startingDate: LocalDate, endingDate: LocalDate): TradedVolumeResponseDTO {
        val transactions = transactionRepository.findAllByStatusAndCompletionDateBetween(
            TransactionStatus.COMPLETED,
            startingDate,
            endingDate
        )
        val amountInARS = transactions.sumOf { it.amountInARS!! }
        val amountInUSD = transactions.sumOf { it.amountInUSD!! }
        return TradedVolumeResponseDTO(startingDate.toString(), endingDate.toString(), amountInUSD, amountInARS)
    }

}
