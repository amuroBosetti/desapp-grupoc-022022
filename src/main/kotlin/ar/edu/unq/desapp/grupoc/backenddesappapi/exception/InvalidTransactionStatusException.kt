package ar.edu.unq.desapp.grupoc.backenddesappapi.exception

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionAction
import ar.edu.unq.desapp.grupoc.backenddesappapi.model.TransactionStatus

class InvalidTransactionStatusException(status: TransactionStatus, action: TransactionAction) :
    RuntimeException("Invalid status $status for action $action")
