package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.lang.Exception
import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.DomainType
import com.binance.api.client.domain.market.TickerPrice
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*


@Controller
class QuotationsController {
    @GetMapping
    @RequestMapping("/tokenPrice/{ticker}")
    @Operation(summary = "Get a token price")
    @ResponseBody
    fun getTokenPrice(@PathVariable ticker: String): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }
}