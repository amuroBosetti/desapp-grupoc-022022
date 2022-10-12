package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table
class CryptoSymbol(

    @Column
    val symbol: String) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null
}