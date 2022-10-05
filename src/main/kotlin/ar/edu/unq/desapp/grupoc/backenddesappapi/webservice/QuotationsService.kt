package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.DomainType
import com.binance.api.client.domain.market.TickerPrice
import org.springframework.stereotype.Service

@Service
class QuotationsService {
    fun getTokenPrice(ticker: String): TickerPrice {
        val factory: BinanceApiClientFactory = BinanceApiClientFactory.newInstance(DomainType.Com)
        val client: BinanceApiRestClient = factory.newRestClient(DomainType.Com)
        return client.getPrice(ticker)
    }
}
