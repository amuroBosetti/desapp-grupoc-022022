package ar.edu.unq.desapp.grupoc.backenddesappapi.exception

import java.util.*

class TransactionWithSameUserInBothSidesException(id: UUID) :
    RuntimeException("Cannot process transaction with id $id because both sides are the same user")
