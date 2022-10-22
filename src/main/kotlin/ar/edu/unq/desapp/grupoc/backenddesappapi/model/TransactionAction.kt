package ar.edu.unq.desapp.grupoc.backenddesappapi.model

import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.TransactionWithSameUserInBothSidesException
import ar.edu.unq.desapp.grupoc.backenddesappapi.exception.UnauthorizedUserForAction

enum class TransactionAction {
    ACCEPT {
        override fun processWith(
            transaction: Transaction,
            user: BrokerUser,
            latestQuotation: Double,
            broker: Broker
        ): Transaction {
            broker.validatePriceBand(transaction, latestQuotation)
            validateUsers(transaction, user)
            return transaction.accept(user, latestQuotation)
        }

        private fun validateUsers(transaction: Transaction, user: BrokerUser) {
            if (transaction.firstUser == user) {
                throw TransactionWithSameUserInBothSidesException(transaction.id!!)
            }
        }

    },
    INFORM_TRANSFER {
        override fun processWith(
            transaction: Transaction,
            user: BrokerUser,
            latestQuotation: Double,
            broker: Broker
        ): Transaction {
            validateUsers(transaction, user)
            return broker.informTransfer(transaction, user)
        }

        private fun validateUsers(transaction: Transaction, user: BrokerUser) {
            if (transaction.operationType == OperationType.BUY && transaction.firstUser != user) {
                throw UnauthorizedUserForAction(user.email, this, transaction.id!!)
            }
        }
    },
    CONFIRM_TRANSFER_RECEPTION {
        override fun processWith(
            transaction: Transaction,
            user: BrokerUser,
            latestQuotation: Double,
            broker: Broker
        ): Transaction {
            validateUsers(transaction, user)
            return broker.confirmTransferReception(transaction.id!!)
        }

        private fun validateUsers(transaction: Transaction, user: BrokerUser) {
            if (transaction.operationType == OperationType.BUY && transaction.secondUser != user
                || transaction.operationType == OperationType.SELL && transaction.firstUser != user){
                throw UnauthorizedUserForAction(user.email, this, transaction.id!!)
            }
        }
    },
    INFORM_CRYPTO_TRANSFER {
        override fun processWith(
            transaction: Transaction,
            user: BrokerUser,
            latestQuotation: Double,
            broker: Broker
        ): Transaction {
            validateUsers(transaction, user)
            return broker.informCryptoTransfer(transaction.id!!)
        }

        private fun validateUsers(transaction: Transaction, user: BrokerUser) {
            if (transaction.operationType == OperationType.SELL && transaction.firstUser != user
                || transaction.operationType == OperationType.BUY && transaction.secondUser != user){
                throw UnauthorizedUserForAction(user.email, this, transaction.id!!)
            }
        }
    },
    CONFIRM_CRYPTO_TRANSFER_RECEPTION {
        override fun processWith(
            transaction: Transaction,
            user: BrokerUser,
            latestQuotation: Double,
            broker: Broker
        ): Transaction {
            validateUsers(transaction, user)
            return broker.confirmCryptoTransferReception(transaction.id!!)
        }

        private fun validateUsers(transaction: Transaction, user: BrokerUser) {
            if (transaction.operationType == OperationType.BUY && transaction.firstUser != user
                || transaction.operationType == OperationType.SELL && transaction.secondUser != user){
                throw UnauthorizedUserForAction(user.email, this, transaction.id!!)
            }
        }
    };

    abstract fun processWith(
        transaction: Transaction,
        user: BrokerUser,
        latestQuotation: Double,
        broker: Broker
    ): Transaction

}
