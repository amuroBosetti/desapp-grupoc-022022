package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Broker
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.TransactionRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.UserRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationResponseDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TransactionService {

    private lateinit var broker: Broker

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @PostConstruct
    fun init() {
        broker = Broker(hashMapOf(Pair("BNBUSDT", 15.0)), 5.0, transactionRepository)
    }

    fun createTransaction(userEmail: String, transactionCreationDTO: TransactionCreationDTO):
            TransactionCreationResponseDTO {
        val user = findUser(userEmail)
        val savedTransaction = broker.expressOperationIntent(
                user, transactionCreationDTO.operationType, transactionCreationDTO.intendedPrice, transactionCreationDTO.symbol
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

    fun processTransaction() : Transaction {
        TODO("Not yet implemented")
    }

}
