package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.InvalidTransactionStatusException

enum class TransactionStatus {
    PENDING {
        override fun informTransfer(transaction: Transaction): TransactionStatus {
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

        override fun informTransfer(transaction: Transaction): TransactionStatus {
            if (transaction.operationType == OperationType.BUY) {
                throw InvalidTransactionStatusException(this, TransactionAction.INFORM_TRANSFER)
            } else {
                return WAITING_CONFIRMATION
            }
        }
    },
    WAITING_CONFIRMATION {
        override fun confirmReception(): TransactionStatus {
            return COMPLETED
        }

        override fun confirmTransferReception(): TransactionStatus {
            return PENDING_CRYPTO_TRANSFER
        }
    },
    COMPLETED,
    CANCELLED;

    open fun confirmReception(): TransactionStatus {
        throw InvalidTransactionStatusException(this, TransactionAction.CONFIRM_TRANSFER_RECEPTION)
    }

    open fun confirmTransferReception(): TransactionStatus {
        throw InvalidTransactionStatusException(this, TransactionAction.CONFIRM_TRANSFER_RECEPTION)
    }

    open fun informTransfer(transaction: Transaction): TransactionStatus {
        throw InvalidTransactionStatusException(this, TransactionAction.INFORM_TRANSFER)
    }

    open fun accept(): TransactionStatus {
        throw InvalidTransactionStatusException(this, TransactionAction.ACCEPT)
    }

    open fun informCryptoTransfer(): TransactionStatus {
        throw InvalidTransactionStatusException(this, TransactionAction.INFORM_CRYPTO_TRANSFER)
    }

    open fun confirmCryptoTransferReception(): TransactionStatus {
        throw InvalidTransactionStatusException(this, TransactionAction.CONFIRM_CRYPTO_TRANSFER_RECEPTION)
    }

}
