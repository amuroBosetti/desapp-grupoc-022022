package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionNotFoundException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionWithSameUserInBothSidesException
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.TransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Controller
class TransactionController {

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

    @RequestMapping("/transaction", method = [RequestMethod.POST])
    fun createTransaction(
        @RequestHeader("user") userEmail: String,
        @RequestBody @Valid transactionCreationDTO: TransactionCreationDTO
    ): ResponseEntity<TransactionCreationResponseDTO> {
        return try {
            ResponseEntity(transactionService.createTransaction(userEmail, transactionCreationDTO), HttpStatus.CREATED)
        } catch (e: NotRegisteredUserException) {
            ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
    }

    @RequestMapping("/transaction/{id}", method = [RequestMethod.PUT])
    fun processTransaction(
        @RequestHeader("user") userEmail: String,
        @PathVariable id: UUID,
        @RequestBody transactionUpdateRequestDTO: TransactionUpdateRequestDTO
    ): ResponseEntity<TransactionUpdateResponseDTO> {
        return try {
            val transaction = transactionService.processTransaction(
                id,
                userEmail
            )
            ResponseEntity(TransactionUpdateResponseDTO(transaction.status), HttpStatus.OK)
        } catch (e: TransactionWithSameUserInBothSidesException) {
            throw HTTPClientException(e.message!!, HttpStatus.BAD_REQUEST)
        } catch (e: TransactionNotFoundException) {
            throw HTTPClientException(e.message!!, HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping("/transaction/active", method = [RequestMethod.GET])
    fun getActiveTransactions(): ResponseEntity<List<ActiveTransactionDTO>> {
        val transactionList = transactionService.getActiveTransactions().map { ActiveTransactionDTO.from(it) }
        return ResponseEntity(transactionList, HttpStatus.OK)
    }

}