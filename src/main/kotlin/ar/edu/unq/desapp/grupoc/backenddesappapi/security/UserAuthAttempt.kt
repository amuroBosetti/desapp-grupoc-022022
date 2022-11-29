package ar.edu.unq.desapp.grupoc.backenddesappapi.security

data class UserAuthAttempt(val userEmail: String, val password: String){
    override fun toString(): String {
        return "UserAuthAttempt(userEmail='$userEmail')"
    }
}

