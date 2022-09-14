package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class User(
    email: String,
    name: String,
    surname: String,
    address: String,
    password: String,
    cvu: String,
    walletAddress: String
) {
    private var reputationPoints: Double = 0.0

    private val EMAIL_REGEX = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"

    lateinit var email: String

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
}