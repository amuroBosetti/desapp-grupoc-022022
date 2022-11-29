package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import ar.edu.unq.desapp.grupoc.backenddesappapi.security.JWTProvider
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Aspect
@Component
@Order(0)
class WithLoggedInUserAspectAnnotation {

    @Autowired
    private lateinit var context : HttpServletRequest

    @Autowired
    private lateinit var jwtProvider: JWTProvider

    @Before("@annotation(WithLoggedUser)")
    fun validateUserCredentials() {
        if (!validateToken()) {
            throw BadCredentialsException("Bad credentials")
        }
    }

    fun validateToken(): Boolean {
        if (!hasToken() || !jwtProvider.isValid(getToken())) {
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