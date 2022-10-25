package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

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
import kotlin.test.assertFailsWith


@SpringBootTest
class QuotationsServiceTest {

    @Autowired
    lateinit var service: QuotationsService
    @MockkBean
    lateinit var client: BinanceApiRestClient
    val mockPrice = "3.00"
    val mockSymbol = "ALICEUSDT"


    @BeforeEach
    fun setUp(){
        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice
        tickerPrice.symbol = mockSymbol

        every { client.getPrice(mockSymbol) } returns  tickerPrice
        every { client.getPrice("") } throws RuntimeException("Could not get the token price")
    }

    @Test
    fun `when asked a token price it retrieves its price`(){

        val tickerPriceResponse = service.getTokenPrice(mockSymbol)

        assertThat(tickerPriceResponse.price).isEqualTo(mockPrice)
        assertThat(tickerPriceResponse.symbol).isEqualTo(mockSymbol)
    }

    @Test
    fun `when asked a token price but it fails then it throws a bad status`(){

        assertFailsWith<RuntimeException>(
            message = "Could not get the token price",
            block = {
                service.getTokenPrice("")
            }
        )
    }
}