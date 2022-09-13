package ar.edu.unq.desapp.grupoc.backenddesappapi.model

enum class TransactionStatus {
    PENDING {
        override fun informTransfer(): TransactionStatus{
            return WAITING_CONFIRMATION
        }
    },
    ACTIVE {
        override fun accept(): TransactionStatus {
            return PENDING
        }
    },
    WAITING_CONFIRMATION {
        override fun confirmReception() : TransactionStatus{
            return COMPLETED
        }
    },
    COMPLETED,
    CANCELLED;

    open fun confirmReception(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("CONFIRM RECEPTION"))
    }

    open fun informTransfer(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("INFORM TRANSFER"))
    }

    open fun accept(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("ACCEPT TRANSACTION"))
    }

    private fun invalidStatusExceptionMessage(action: String) = "Invalid status $this for action $action"

}
