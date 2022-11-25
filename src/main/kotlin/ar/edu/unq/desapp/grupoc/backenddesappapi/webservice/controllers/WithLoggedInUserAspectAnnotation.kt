package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import ar.edu.unq.desapp.grupoc.backenddesappapi.security.JWTProvider
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Aspect
@Component
class WithLoggedInUserAspectAnnotation {

    @Autowired
    private lateinit var context : HttpServletRequest

    @Autowired
    private lateinit var jwtProvider: JWTProvider

    @Around("@annotation(WithLoggedUser)")
    fun validateUserCredentials(joinPoint: ProceedingJoinPoint): ResponseEntity<Any> {
        if (!validateToken()) {
            throw BadCredentialsException("Bad credentials")
        }
        return joinPoint.proceed() as ResponseEntity<Any>
    }

    fun validateToken(): Boolean {
        if (!hasToken()) {
            return false
        }
        if (!jwtProvider.isValid(getToken())) {
            return false
        }
        context.session.setAttribute("user", jwtProvider.getEmailFromToken(getToken()))
        return true
    }

    private fun getToken(): String {
        return context.getHeader("authorization")
    }

    private fun hasToken(): Boolean =
        context.headerNames.toList().any { it.equals("authorization") }

}