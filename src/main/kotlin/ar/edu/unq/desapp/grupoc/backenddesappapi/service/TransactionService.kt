package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionNotFoundException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Broker
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionAction
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationResponseDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
        broker = Broker(5.0, transactionRepository, quotationsService)
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

}
