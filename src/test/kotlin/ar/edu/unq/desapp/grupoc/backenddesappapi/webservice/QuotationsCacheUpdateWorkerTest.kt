package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers.HighPerformanceQuotationsController
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import java.util.*

@SpringBootTest
class QuotationsCacheUpdateWorkerTest {

    @MockkBean
    lateinit var highPerformanceQuotationsController: HighPerformanceQuotationsController

    @Autowired
    lateinit var worker: QuotationsCacheUpdateWorker

    @Test
    fun `when the scheduled event is called, then the cache is refreshed`() {
        every { highPerformanceQuotationsController.getAllTokenPrices() }.returns(ResponseEntity.of(Optional.of(listOf())))

        worker.refreshCache()

        verify { highPerformanceQuotationsController.getAllTokenPrices() }
    }

}