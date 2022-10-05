package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.DomainType
import com.binance.api.client.domain.market.TickerPrice
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class QuotationsServiceTest {

    @Autowired
    lateinit var service: QuotationsService
    @MockK
    lateinit var factory: BinanceApiClientFactory
    @MockK
    lateinit var client: BinanceApiRestClient
    val mockPrice = "3.00"
    val mockSymbol = "ALICEUSDT"


    @BeforeEach
    fun setUp(){
        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice
        tickerPrice.symbol = mockSymbol

        every { factory.newRestClient(DomainType.Com) } returns client
        every { client.getPrice(any()) } returns tickerPrice
    }

    @Test
    fun `when asked a token price it retrieves its price`(){

        val tickerPriceResponse = service.getTokenPrice(mockSymbol)

        assertThat(tickerPriceResponse.price).isEqualTo(mockPrice)
        assertThat(tickerPriceResponse.symbol).isEqualTo(mockSymbol)
    }
}