package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.TickerPriceDTO
import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.DomainType
import com.binance.api.client.domain.market.TickerPrice
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class QuotationsService {

    val tickers = mutableListOf<String>("ALICEUSDT",
        "MATICUSDT",
        "AXSUSDT",
        "AAVEUSDT",
        "ATOMUSDT",
        "NEOUSDT",
        "DOTUSDT",
        "ETHUSDT",
        "CAKEUSDT",
        "BTCUSDT",
        "BNBUSDT",
        "ADAUSDT",
        "TRXUSDT",
        "AUDIOUSDT",
    )

    @Autowired
    lateinit var client: BinanceApiRestClient

    @Autowired
    lateinit var factory: BinanceApiClientFactory

    fun getTokenPrice(ticker: String?): TickerPriceDTO {
    try {
            return TickerPriceDTO(
                symbol = client.getPrice(ticker).symbol,
                price = client.getPrice(ticker).price)
        } catch (e: Exception){
            throw RuntimeException("Could not get the token price")
        }
    }

    fun getAllTokenPrices(): List<TickerPriceDTO> {
        try {
            return client.getAllPrices()
                .filter { tickerPrice -> tickers.contains(tickerPrice.symbol) }
                .map { tickerPrice -> TickerPriceDTO(symbol = tickerPrice.symbol, price = tickerPrice.price) }
        } catch (e: Exception) {
            throw RuntimeException("Could not get the token prices")
        }
    }
}

