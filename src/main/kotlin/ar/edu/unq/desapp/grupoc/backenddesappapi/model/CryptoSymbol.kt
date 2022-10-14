package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity(name = "crypto_symbols")
@Table
class CryptoSymbol(

    @Column
    val symbol: String) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}