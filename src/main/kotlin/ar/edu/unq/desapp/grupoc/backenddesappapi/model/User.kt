package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class User(
    val email: String,
    val name: String,
    val surname: String,
    val address: String,
    val password: String,
    val cvu: String,
    val walletId: String
) {
    private var reputationPoints: Double = 0.0

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