package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY
import javax.validation.constraints.Size

@Entity
@Table
class BrokerUser(
    @Column(unique = true, nullable = false)
    val email: String,
    @Column(nullable = false)
    @Size(min = 3, max = 30)
    val name: String,
    @Column(nullable = false)
    @Size(min = 3, max = 30)
    val surname: String,
    @Column(nullable = false)
    @Size(min = 10, max = 30)
    val address: String,
    @Column(nullable = false)
    val password: String,
    @Column(unique = true)
    @Size(min = 22, max = 22)
    val cvu: String,
    @Size(min = 8, max = 8)
    @Column(unique = true)
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
        if (!EMAIL_REGEX.toRegex().matches(email)) {
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
        if (reputationPoints < amount) {
            reputationPoints = 0.0
        } else {
            reputationPoints -= amount
        }
    }
}