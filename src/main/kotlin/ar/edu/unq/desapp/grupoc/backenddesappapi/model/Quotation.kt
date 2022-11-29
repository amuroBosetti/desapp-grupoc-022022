package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
@Table
data class Quotation(
    @Column
    val symbol: String,
    @Column
    val price: String,
    @Column
    val dateTime: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    var id: Long? = null
}