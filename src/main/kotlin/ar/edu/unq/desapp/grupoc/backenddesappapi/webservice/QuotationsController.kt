package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice;
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.lang.Exception
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@Controller
class QuotationsController {

    @Autowired(required = true)
    lateinit var quotationsService: QuotationsService

    @GetMapping
    @RequestMapping("/tokenPrice/{ticker}")
    @Operation(summary = "Get a token price")
    @ResponseBody
    fun getTokenPrice(@PathVariable ticker: String): ResponseEntity<TickerPriceDTO> {
        return try {
            ResponseEntity(quotationsService.getTokenPrice(ticker), HttpStatus.OK)
        } catch (e: Exception){
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping
    @RequestMapping("/tokenPrices")
    @Operation(summary = "Get all listed token prices")
    @ResponseBody
    fun getAllTokenPrices(): ResponseEntity<String> {
        return try {
            ResponseEntity(quotationsService.getAllTokenPrices().toString(), HttpStatus.OK)
        } catch (e: Exception){
            ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }
}