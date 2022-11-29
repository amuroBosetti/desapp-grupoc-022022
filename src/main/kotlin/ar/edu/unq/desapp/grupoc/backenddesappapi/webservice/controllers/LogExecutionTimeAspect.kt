package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers;

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component;
import java.time.Duration
import java.time.Instant

@Aspect
@Component
public class LogExecutionTimeAspect {

    val logger = LoggerFactory.getLogger(LogExecutionTimeAspect::class.java)

    @Around("@annotation(LogExecTime)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = Instant.now()
        logger.info("TIMESTAMP: ${startTime}")
        val result = joinPoint.proceed()
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        logger.info("EXECUTION TIME: ${duration.toMillis()} MS")
        return result
    }
}
