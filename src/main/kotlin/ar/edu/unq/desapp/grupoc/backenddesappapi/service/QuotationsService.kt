package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.DomainType
import com.binance.api.client.domain.market.TickerPrice
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class QuotationsService {

    @Autowired
    lateinit var client: BinanceApiRestClient

    @Autowired
    lateinit var factory: BinanceApiClientFactory

    fun getTokenPrice(ticker: String?): TickerPrice {
    try {
//            factory = BinanceApiClientFactory.newInstance(DomainType.Com)
//            client = factory.newRestClient(DomainType.Com)
            return client.getPrice(ticker)
        } catch (e: Exception){
            throw RuntimeException("Could not get the token price")
        }
    }
}
