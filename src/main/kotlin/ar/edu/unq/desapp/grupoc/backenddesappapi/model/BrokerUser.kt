package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import javax.persistence.*
import javax.persistence.GenerationType.*
import kotlin.jvm.Transient

@Entity
@Table
class BrokerUser(
    @Column
    val email: String,
    @Column
    val name: String,
    @Column
    val surname: String,
    @Column
    val address: String,
    @Column
    val password: String,
    @Column
    val cvu: String,
    @Column
    val walletId: String
) {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null

    @Column
    private var reputationPoints: Double = 0.0

    @Transient
    private val EMAIL_REGEX = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"

    init {
        validateEmail(email)
    }

    private fun validateEmail(email: String) {
        if (!EMAIL_REGEX.toRegex().matches(email)){
            throw RuntimeException("Invalid email")
        }
    }

    fun getReputationPoints(): Double {
        return reputationPoints
    }

    fun increaseReputationPoints(amount: Double) {
        reputationPoints += amount
    }

    fun subtractReputationPoints(amount: Double) {
        if (reputationPoints < amount){
            reputationPoints = 0.0
        } else {
            reputationPoints -= amount
        }
    }
}