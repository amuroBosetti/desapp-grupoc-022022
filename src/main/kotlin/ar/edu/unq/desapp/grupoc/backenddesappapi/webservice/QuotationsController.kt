package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.lang.Exception
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@Controller
class QuotationsController {

    @Autowired
    lateinit var quotationsService: QuotationsService

    @GetMapping
    @RequestMapping("/tokenPrice/{ticker}")
    @Operation(summary = "Get a token price")
    @ResponseBody
    fun getTokenPrice(@PathVariable ticker: String): ResponseEntity<String> {
        return try {
            ResponseEntity(quotationsService.getTokenPrice(ticker).toString(), HttpStatus.OK)
        } catch (e: Exception){
            ResponseEntity(e.toString(), HttpStatus.BAD_REQUEST)
        }
    }
}