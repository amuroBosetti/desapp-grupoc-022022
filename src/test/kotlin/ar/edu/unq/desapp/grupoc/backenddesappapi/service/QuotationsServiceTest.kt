package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.CouldNotFindTokenException
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.Quotation
import ar.edu.unq.desapp.grupoc.backenddesappapi.repository.QuotationRepository
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.TickerPrice
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.*
import kotlin.test.assertFailsWith


@SpringBootTest
class QuotationsServiceTest {

    @Autowired
    lateinit var service: QuotationsService
    @MockkBean(relaxed = true)
    lateinit var client: BinanceApiRestClient
    val mockPrice = "3.00"
    val mockSymbol = "ALICEUSDT"
    @MockkBean
    lateinit var clock: Clock
    @Autowired
    lateinit var quotationRepository: QuotationRepository
    val dateTime = LocalDateTime.of(2022, Month.OCTOBER, 1, 9, 0, 0).toString()
    val dateTimeQ = LocalDateTime.of(2022, Month.OCTOBER, 1, 8, 0, 0)


    @BeforeEach
    fun setUp(){
        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice
        tickerPrice.symbol = mockSymbol

        val quotation = Quotation(mockSymbol, mockPrice, dateTimeQ.toString())
        quotationRepository.save(quotation)

        every { clock.instant() } returns LocalDateTime.parse(dateTime).toInstant(ZoneOffset.UTC)
        every { clock.zone } returns ZoneId.of("GMT-3")
        every { client.getPrice(mockSymbol) } returns  tickerPrice
        every { client.getPrice("") } throws CouldNotFindTokenException()
        every { client.getAllPrices() } returns listOf(tickerPrice)
    }

    @Test
    fun `when asked a token price it retrieves its price`(){

        val tickerPriceResponse = service.getTokenPrice(mockSymbol)

        assertThat(tickerPriceResponse.price).isEqualTo(mockPrice)
        assertThat(tickerPriceResponse.symbol).isEqualTo(mockSymbol)
    }

    @Test
    fun `when asked a token price but it fails then it throws a bad status`(){

        assertFailsWith<CouldNotFindTokenException>(
            message = "Could not get the token price",
            block = {
                service.getTokenPrice("")
            }
        )
    }

    @Test
    fun `when asked the 24hs history prices of a token it retrieves only those`(){
        val priceHistory = service.get24HsPrice(mockSymbol)

        assertThat(LocalDateTime.parse(priceHistory[0].dateTime)).isBeforeOrEqualTo(dateTime)
        assertThat(priceHistory[0].price).isEqualTo(mockPrice)
        assertThat(priceHistory[0].symbol).isEqualTo(mockSymbol)
    }
}