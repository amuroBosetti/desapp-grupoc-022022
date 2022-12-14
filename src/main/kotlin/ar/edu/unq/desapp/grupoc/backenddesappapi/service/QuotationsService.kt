package ar.edu.unq.desapp.grupoc.backenddesappapi.service

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.CouldNotFindTokenException
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers.TransactionController
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Quotation
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.QuotationRepository
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.exception.BinanceApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.streams.toList


@Service
class QuotationsService(val clock: Clock) {

    //Todo: Move and fetch from DB
    val tickers = mutableListOf(
        "ALICEUSDT",
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
    lateinit var quotationRepository: QuotationRepository

    @Autowired
    lateinit var client: BinanceApiRestClient

    fun getTokenPrice(symbol: String?): Quotation {
        try {
            val tickerPrice = client.getPrice(symbol)
            val dateTime = LocalDateTime.now().toString()
            val quotation = Quotation(tickerPrice.symbol, tickerPrice.price, dateTime)
            quotationRepository.save(quotation)
            return quotation
        } catch (e: BinanceApiException) {
            throw CouldNotFindTokenException()
        }
    }

    @Scheduled(fixedRate = 600000)
    fun saveTokenPrices() {
        quotationRepository.saveAll(getAllTokenPrices())
    }

    fun getAllTokenPrices(): List<Quotation> {
        try {
            val dateTime = LocalDateTime.now().toString()
            return client.allPrices
                .filter { tickerPrice -> tickers.contains(tickerPrice.symbol) }
                .map { tickerPrice ->
                    val q = Quotation(symbol = tickerPrice.symbol, price = tickerPrice.price, dateTime)
                    quotationRepository.save(q)
                }
        } catch (e: BinanceApiException) {
            throw CouldNotFindTokenException()
        }
    }

    fun get24HsPrice(symbol: String): List<Quotation> {
        val now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC)
        return quotationRepository.findBySymbolOrderByDateTimeDesc(symbol).stream().filter {
            isWithinPrior24Hours(
                LocalDateTime.parse(it.dateTime), now
            )
        }.toList()
    }

    fun isWithinPrior24Hours(aDateTime: LocalDateTime, now: LocalDateTime): Boolean {
        return !aDateTime.isBefore(now.minusDays(1))
                &&
                (aDateTime.isBefore(now) || aDateTime.isEqual(now))
    }
}

