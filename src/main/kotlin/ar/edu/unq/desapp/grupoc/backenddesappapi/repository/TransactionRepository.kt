package ar.edu.unq.desapp.grupoc.backenddesappapi.repository

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Repository
@Transactional
interface TransactionRepository : CrudRepository<Transaction, UUID> {
    fun findByFirstUser(user: BrokerUser): List<Transaction>
    fun findAllByStatus(status: TransactionStatus): List<Transaction>
    fun findAllByStatusAndCompletionDateBetween(
        status: TransactionStatus,
        startingDate: LocalDate,
        endingDate: LocalDate
    ): List<Transaction>
}
