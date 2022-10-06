package ar.edu.unq.desapp.grupoc.backenddesappapi.webservice

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.NotRegisteredUserException
import ar.edu.unq.desapp.grupoc.backenddesappapi.service.TransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Controller
class TransactionController {

    @Autowired
    lateinit var transactionService: TransactionService

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(exception: MethodArgumentNotValidException) : ResponseEntity<String>{
        val messages = exception.fieldErrors.map {
            "Field ${it.field} ${it.defaultMessage}"
        }
        return ResponseEntity(messages.toString(),HttpStatus.BAD_REQUEST)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleException(exception: HttpMessageNotReadableException) : ResponseEntity<String>{
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @RequestMapping("/transaction", method = [RequestMethod.POST])
    fun createTransaction(@RequestHeader("user") userEmail : String,  @RequestBody @Valid transactionCreationDTO: TransactionCreationDTO) : ResponseEntity<TransactionCreationResponseDTO> {
        return try {
            ResponseEntity(transactionService.createTransaction(userEmail, transactionCreationDTO), HttpStatus.CREATED)
        } catch (e: NotRegisteredUserException) {
            ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
    }

}