package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class User(email: String) {
    val EMAIL_REGEX = "^[A-Za-z](.*)(@)(.+)(\\.)(.+)"

    lateinit var email: String

    init {
        validateEmail(email)
    }

    private fun validateEmail(email: String) {
        if (!EMAIL_REGEX.toRegex().matches(email)){
            throw RuntimeException("Invalid email")
        }
    }
}