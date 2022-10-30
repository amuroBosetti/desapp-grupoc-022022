package ar.edu.unq.desapp.grupoc.backenddesappapi.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@Component
class JWTProvider {

    // TODO tomar esto de variables de ambiente
    @Value("\${jwt.secret}")
    lateinit var secret: String

    @PostConstruct
    fun initialize() {
        secret = Base64.getEncoder().encodeToString(secret.encodeToByteArray())
    }

    fun createToken(authAttempt: UserAuthAttempt): String {
        val anHour: Long = 3600
        return JWT.create()
            .withSubject(authAttempt.userEmail)
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plusSeconds(anHour))
            .sign(Algorithm.HMAC256(secret))
    }

    fun isValid(token: String): Boolean {
        return try {
            val verifier = getVerifier()
            verifier.verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getEmailFromToken(token: String): String {
        try {
            val verifier = getVerifier()
            return verifier.verify(token).subject
        } catch (e: SignatureVerificationException) {
            throw BadCredentialsException("Bad token")
        }
    }

    private fun getVerifier(): JWTVerifier {
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.require(algorithm)
            .build()
    }

}