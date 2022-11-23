package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.TickerPrice
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class HighPerformanceQuotationsControllerTest {

    @Autowired
    lateinit var mockMvc : MockMvc

    @MockkBean
    lateinit var client: BinanceApiRestClient
    val mockPrice = "3.00"
    val mockSymbol = "ALICEUSDT"

    val tickers = mutableListOf("ALICEUSDT",
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

    @BeforeEach
    fun setUp(){
        val tickerPrice = TickerPrice()
        tickerPrice.price = mockPrice
        tickerPrice.symbol = mockSymbol

        every { client.getAllPrices() } returns tickers.map {
            val t = TickerPrice()
            t.price = "2.05"
            t.symbol = it
            t
        }
    }

    @Test
    fun `when asked all tokens their tickers are retrieved`() {
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/token/fast-prices")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString

        val responseDTO = jacksonObjectMapper().readerForListOf(TickerPriceDTO::class.java)
            .readValue<List<TickerPriceDTO>>(response)
        assertThat(responseDTO).extracting("symbol").containsAll(tickers)
    }

}