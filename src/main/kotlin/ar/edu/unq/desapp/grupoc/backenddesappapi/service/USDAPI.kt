package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.ExchangeRateDTO
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class USDAPI {

    val BASE_URL = "https://api-dolar-argentina.herokuapp.com/api/dolaroficial"

    fun getARSOfficialRate(): ExchangeRateDTO {
        val restTemplate = RestTemplate()
        val responseEntity = restTemplate.getForEntity(
            BASE_URL, ExchangeRateDTO::class.java
        )
        val exchangeRates = responseEntity.body!!
        return exchangeRates
    }

}
