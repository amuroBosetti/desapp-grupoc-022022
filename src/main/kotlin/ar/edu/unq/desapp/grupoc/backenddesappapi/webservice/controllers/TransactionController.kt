package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.controllers

import PriceOutsidePriceBandException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.*
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.InvaliDateFormat
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.TransactionService
import ar.edu.unq.desapp.grupoc.backenddesappapi.webservice.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpSession
import javax.validation.Valid

@Tag(name = "Transactions", description = "Transaction service")
@Controller
class TransactionController : HttpController() {

    @Autowired
    lateinit var transactionService: TransactionService

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(exception: MethodArgumentNotValidException): ResponseEntity<String> {
        val messages = exception.fieldErrors.map {
            "Field ${it.field} ${it.defaultMessage}"
        }
        return ResponseEntity(messages.toString(), HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleException(exception: HttpMessageNotReadableException): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HTTPClientException::class)
    fun handleException(exception: HTTPClientException): ResponseEntity<String> {
        return ResponseEntity(exception.returnMessage, exception.status)
    }

    @ExceptionHandler(InvaliDateFormat::class)
    fun handleException(exception: InvaliDateFormat): ResponseEntity<ErrorDTO> {
        return ResponseEntity(ErrorDTO(exception.message!!, InvaliDateFormat::class.java.simpleName
        ), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleException(exception: BadCredentialsException): ResponseEntity<ErrorDTO> {
        return ResponseEntity(ErrorDTO("Bad credentials", "Bad credentials"
        ), HttpStatus.UNAUTHORIZED)
    }

    @Operation(summary = "Create a new transaction")
    @RequestMapping("/transaction", method = [RequestMethod.POST])
    @WithLoggedUser
    @LogExecTime
    fun createTransaction(
        @RequestBody @Valid transactionCreationDTO: TransactionCreationDTO,
        httpSession: HttpSession
    ): ResponseEntity<TransactionCreationResponseDTO> {
        return try {
            ResponseEntity(transactionService.createTransaction(getUserEmail(httpSession), transactionCreationDTO), HttpStatus.CREATED)
        } catch (e: BadCredentialsException) {
            ResponseEntity(HttpStatus.UNAUTHORIZED)
        } catch (e: UnexpectedUserInformationException) {
            throw HTTPClientException(e.message!!, HttpStatus.BAD_REQUEST)
        } catch (e: PriceOutsidePriceBandException) {
            throw HTTPClientException(e.message!!, HttpStatus.UNPROCESSABLE_ENTITY)
        }
    }


    @Operation(
        summary = "Processes a transaction",
        description = "This endpoint is used to move a transaction from one status to the other, for example when a user has to inform that they have sent money to the other one"
    )
    @RequestMapping("/transaction/{id}", method = [RequestMethod.PUT])
    @WithLoggedUser
    @LogExecTime
    fun processTransaction(
        @PathVariable id: UUID,
        @RequestBody updateRequest: TransactionUpdateRequestDTO,
        httpSession: HttpSession
    ): ResponseEntity<TransactionUpdateResponseDTO> {
        return try {
            val transaction = transactionService.processTransaction(
                id,
                getUserEmail(httpSession),
                updateRequest.action
            )
            ResponseEntity(TransactionUpdateResponseDTO(transaction.status), HttpStatus.OK)
        } catch (e: TransactionWithSameUserInBothSidesException) {
            throw HTTPClientException(e.message!!, HttpStatus.BAD_REQUEST)
        } catch (e: TransactionNotFoundException) {
            throw HTTPClientException(e.message!!, HttpStatus.NOT_FOUND)
        } catch (e: UnauthorizedUserForAction) {
            throw HTTPClientException(e.message!!, HttpStatus.UNPROCESSABLE_ENTITY)
        }
    }

    @RequestMapping("/transaction/active", method = [RequestMethod.GET])
    @WithLoggedUser
    @LogExecTime
    fun getActiveTransactions(): ResponseEntity<List<ActiveTransactionDTO>> {
        val transactionList = transactionService.getActiveTransactions().map { ActiveTransactionDTO.from(it) }
        return ResponseEntity(transactionList, HttpStatus.OK)
    }


    private fun getUserEmail(httpSession: HttpSession) = httpSession.getAttribute("user") as String

    @Operation(
        summary = "Retrieves the amount of volume traded between 2 dates",
        description = "This endpoint receives 2 dates and retrieves the total amount of transactions volume traded in USD and ARS"
    )
    @RequestMapping("/traded/volume", method = [RequestMethod.GET])
    @WithLoggedUser
    @LogExecTime
    fun getTradedVolume(@RequestBody tradedVolumeDTO: TradedVolumeRequestDTO): ResponseEntity<TradedVolumeResponseDTO> {
        val tradedVolumeDTO: TradedVolumeResponseDTO =
            transactionService.getTradedVolume(tradedVolumeDTO.startingDate, tradedVolumeDTO.endingDate)
        return ResponseEntity(tradedVolumeDTO, HttpStatus.OK)
    }

}