package ar.edu.unq.desapp.grupoc.backenddesappapi.model

enum class TransactionStatus {
    PENDING {
        override fun informTransfer(): TransactionStatus{
            return WAITING_CONFIRMATION
        }
    },
    PENDING_CRYPTO_TRANSFER {
        override fun informCryptoTransfer(): TransactionStatus {
            return WAITING_CRYPTO_CONFIRMATION
        }                       
    },
    WAITING_CRYPTO_CONFIRMATION {
        override fun confirmCryptoTransferReception(): TransactionStatus {
            return COMPLETED
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

        override fun confirmTransferReception() : TransactionStatus {
            return PENDING_CRYPTO_TRANSFER
        }
    },
    COMPLETED,
    CANCELLED;

    open fun confirmReception(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("CONFIRM RECEPTION"))
    }

    open fun confirmTransferReception(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("CONFIRM TRANSFER RECEPTION"))
    }

    open fun informTransfer(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("INFORM TRANSFER"))
    }

    open fun accept(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("ACCEPT TRANSACTION"))
    }
    open fun informCryptoTransfer(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("INFORM CRYPTO TRANSFER"))
    }

    open fun confirmCryptoTransferReception(): TransactionStatus {
        throw RuntimeException(invalidStatusExceptionMessage("CONFIRM CRYPTO TRANSFER RECEPTION"))
    }
    private fun invalidStatusExceptionMessage(action: String) = "Invalid status $this for action $action"

}
