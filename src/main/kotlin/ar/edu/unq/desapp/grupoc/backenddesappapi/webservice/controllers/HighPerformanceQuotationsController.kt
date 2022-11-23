package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TickerPriceDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Tag(name = "Quotations", description = "Quotations service")
@Controller
class HighPerformanceQuotationsController {

    @Autowired(required = true)
    lateinit var quotationsService: QuotationsService

    @RequestMapping("/token/fast-prices", method = [RequestMethod.GET])
    @Operation(summary = "Get all listed token prices, with a delay of 10 minutes at most")
    @ResponseBody
    @Cacheable(
        value = ["priceCache"]
    )
    fun getAllTokenPrices(): ResponseEntity<List<TickerPriceDTO>> {
        return try {
            ResponseEntity(quotationsService.getAllTokenPrices(), HttpStatus.OK)
        } catch (e: Exception) {
            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}