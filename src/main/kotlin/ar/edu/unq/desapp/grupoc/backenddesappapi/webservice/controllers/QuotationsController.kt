package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.CouldNotFindTokenException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Quotation
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Tag(name = "Quotations", description = "Quotations service")
@Controller
class QuotationsController : HttpController() {

    val logger = LoggerFactory.getLogger(QuotationsController::class.java)

    @Autowired(required = true)
    lateinit var quotationsService: QuotationsService

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CouldNotFindTokenException::class)
    fun handleException(exception: CouldNotFindTokenException): ResponseEntity<String> {
        logger.error(exception.message)
        return ResponseEntity(exception.message, HttpStatus.BAD_REQUEST)
    }

    @RequestMapping("/token/price/{ticker}", method = [RequestMethod.GET])
    @Operation(summary = "Get a token price")
    @ResponseBody
    @LogExecTime
    fun getTokenPrice(@PathVariable ticker: String): ResponseEntity<Quotation> {
        return ResponseEntity(quotationsService.getTokenPrice(ticker), HttpStatus.OK)
    }

    @RequestMapping("/token/prices", method = [RequestMethod.GET])
    @Operation(summary = "Get all listed token prices")
    @ResponseBody
    @LogExecTime
    fun getAllTokenPrices(): ResponseEntity<List<Quotation>> {
        return try {
            ResponseEntity(quotationsService.getAllTokenPrices(), HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @RequestMapping("/token/prices/24hs/{ticker}", method = [RequestMethod.GET])
    @Operation(summary = "Get prices for the last 24hs of a token")
    @ResponseBody
    @LogExecTime
    fun get24HsToken(@PathVariable ticker: String): ResponseEntity<
            List<Quotation>> {
        return try {
            ResponseEntity(quotationsService.get24HsPrice(ticker), HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}