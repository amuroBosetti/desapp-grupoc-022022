package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.QuotationsService
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.UserService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class E2ETest {

    @LocalServerPort
    private var port: Number = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `when the price of a token is requested then it retrieves it from binance`() {
        val symbol = "BNBUSDT"
        val response = restTemplate.getForObject(
            "http://localhost:" + port + "/token/price/${symbol}",
            TickerPriceDTO::class.java
        )
        assertThat(response.symbol).contains(symbol)
        assertThat(response.price).isNotNull()
    }

    @Test
    fun `when all listed tokens are asked their prices are retrieved`() {
        val response = restTemplate.getForObject(
            "http://localhost:$port/token/prices",
            String::class.java
        )
        val responseDTO = jacksonObjectMapper().readerForListOf(TickerPriceDTO::class.java)
            .readValue<List<TickerPriceDTO>>(response)
        assertThat(responseDTO.map { it.symbol }.containsAll(QuotationsService().tickers)).isTrue
        assertThat(responseDTO.map { it.price }.all { it.isNotBlank()}).isTrue
    }

    @Test
    fun `when a post request is received for creating a user then it returns the saved user`(){
        val userCreationPayload = UserCreationDTO("pepe",
            "argento",
            "pepe@gmail.com",
            "calle falsa 1234",
            "Password123!",
            "7987818411100011451153",
            "12345678")
        val responseDTO = restTemplate.postForObject(
            "http://localhost:$port/user", userCreationPayload,
            UserCreationResponseDTO::class.java
        )

        assertThat(responseDTO).usingRecursiveComparison()
            .ignoringFields("userId", "password")
            .isEqualTo(userCreationPayload)
        assertThat(responseDTO.userId).isNotNull
        assertThat(responseDTO.name).isNotNull
    }

    @Test
    fun `when a request is received for creating a user with an invalid password then it returns an error`(){
        val userCreationPayload = UserCreationDTO("pepe",
            "argento",
            "pepe@gmail.com",
            "calle falsa 1234",
            "",
            "7987818411100011451153",
            "12345678")
        val response = restTemplate.postForObject(
            "http://localhost:$port/user", userCreationPayload,
            String::class.java
        )

        assertThat(response).contains("Field password must not be blank")
    }
}