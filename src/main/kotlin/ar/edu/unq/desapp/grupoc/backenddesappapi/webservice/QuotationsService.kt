package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.TickerPrice
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class QuotationsService {

    @Autowired
    lateinit var client: BinanceApiRestClient

    fun getTokenPrice(ticker: String): TickerPrice {
        return client.getPrice(ticker)
    }
}
