package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice;
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.CouldNotFoundTokenException
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@Controller
class QuotationsController {

    @Autowired(required = true)
    lateinit var quotationsService: QuotationsService

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CouldNotFoundTokenException::class)
    fun handleException(exception: CouldNotFoundTokenException) : ResponseEntity<String>{
        return ResponseEntity(exception.message, HttpStatus.BAD_REQUEST)
    }

    @GetMapping
    @RequestMapping("/token/price/{ticker}")
    @Operation(summary = "Get a token price")
    @ResponseBody
    fun getTokenPrice(@PathVariable ticker: String): ResponseEntity<TickerPriceDTO> {
        return ResponseEntity(quotationsService.getTokenPrice(ticker), HttpStatus.OK)
    }

    @GetMapping
    @RequestMapping("/token/prices")
    @Operation(summary = "Get all listed token prices")
    @ResponseBody
    fun getAllTokenPrices(): ResponseEntity<List<TickerPriceDTO>> {
        return try {
            ResponseEntity(quotationsService.getAllTokenPrices(), HttpStatus.OK)
        } catch (e: Exception){
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}