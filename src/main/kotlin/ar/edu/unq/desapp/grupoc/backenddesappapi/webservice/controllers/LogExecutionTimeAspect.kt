package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpSession

@Aspect
@Component
@Order(1)
class LogExecutionTimeAspect {

    @Around("@annotation(LogExecTime)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val logger: Logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)

        logHttpMethod(joinPoint, logger)
        logSessionUser(joinPoint, logger)

        val startTime = Instant.now()
        logger.info("TIMESTAMP: $startTime")
        val result = joinPoint.proceed()
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        logger.info("EXECUTION TIME: ${duration.toMillis()} MS")
        return result
    }

    private fun logHttpMethod(joinPoint: ProceedingJoinPoint, logger: Logger) {
        val methodSignature: MethodSignature = joinPoint.signature as MethodSignature
        val annotation = methodSignature.method.getAnnotation(RequestMapping::class.java)
        val argumentsToLog = joinPoint.args.filterNot{ (HttpSession::class.java).isAssignableFrom(it.javaClass) }.map { it.toString() }
        logger.info("HANDLING REQUEST FOR ${annotation.method.first()} TO ${annotation.value.first()} WITH PARAMETERS $argumentsToLog")
    }

    private fun logSessionUser(joinPoint: ProceedingJoinPoint, logger: Logger) {
        val maybeUser = Optional.ofNullable(joinPoint.args.filter { (HttpSession::class.java).isAssignableFrom(it.javaClass) }
            .map { (it as HttpSession).getAttribute("user") }.firstOrNull())
        maybeUser.ifPresent { logger.info("LOGGED USER EMAIL: $it") }
    }
}
