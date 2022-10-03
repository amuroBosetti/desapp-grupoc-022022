package ar.edu.unq.desapp.grupoc.backenddesappapi.repository

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Transaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
interface TransactionRepository : CrudRepository<Transaction, UUID>
