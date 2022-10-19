package ar.edu.unq.desapp.grupoc.backenddesappapi.exception

import java.util.*

class TransactionNotFoundException(operationId: UUID) : RuntimeException("Could not find operation with id $operationId")
