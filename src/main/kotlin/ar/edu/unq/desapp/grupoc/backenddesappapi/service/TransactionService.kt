package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationDTO
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TransactionCreationResponseDTO
import org.springframework.stereotype.Service

@Service
class TransactionService {
    fun createTransaction(userEmail: String, transactionCreationDTO: TransactionCreationDTO) : TransactionCreationResponseDTO{
        TODO("Not yet implemented")
    }

}
