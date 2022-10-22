package ar.edu.unq.desapp.grupoc.backenddesappapi.exception

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionAction
import java.util.*

class UnauthorizedUserForAction(email: String, action: TransactionAction, transactionId: UUID) :
    RuntimeException("User $email is not authorized to perform action $action on transaction $transactionId")
