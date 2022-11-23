package ar.edu.unq.desapp.grupoc.backenddesappapi.config

import ar.edu.unq.desapp.grupoc.backenddesappapi.security.JWTProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthInterceptor : HandlerInterceptor {

    @Autowired
    private lateinit var jwtProvider: JWTProvider

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (!hasToken(request)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing authentication token")
            return false
        }
        if (!jwtProvider.isValid(getToken(request))) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Bad credentials")
            return false
        }
        request.session.setAttribute("user", jwtProvider.getEmailFromToken(getToken(request)))
        return true
    }

    private fun getToken(request: HttpServletRequest): String {
        return request.getHeader("authorization")
    }

    private fun hasToken(request: HttpServletRequest): Boolean =
        request.headerNames.toList().any { it.equals("authorization") }

}