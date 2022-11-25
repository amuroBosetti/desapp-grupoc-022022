package ar.edu.unq.desapp.grupoc.backenddesappapi.repository


import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Quotation
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
interface QuotationRepository : CrudRepository<Quotation, UUID> {
    fun findBySymbolOrderByDateTimeDesc(symbol: String): List<Quotation>
}