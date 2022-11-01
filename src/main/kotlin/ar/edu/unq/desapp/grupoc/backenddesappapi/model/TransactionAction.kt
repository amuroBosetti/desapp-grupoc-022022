package ar.edu.unq.desapp.grupoc.backenddesappapi.model

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

        override fun usersAreNotValidForAction(transaction: Transaction, user: BrokerUser): Boolean =
            transaction.firstUser == user

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

        override fun usersAreNotValidForAction(transaction: Transaction, user: BrokerUser): Boolean =
            transaction.operationType == OperationType.BUY && transaction.firstUser != user || transaction.operationType == OperationType.SELL && transaction.firstUser == user
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

        override fun usersAreNotValidForAction(transaction: Transaction, user: BrokerUser): Boolean =
            transaction.operationType == OperationType.BUY && transaction.secondUser != user
                    || transaction.operationType == OperationType.SELL && transaction.firstUser != user
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

        override fun usersAreNotValidForAction(transaction: Transaction, user: BrokerUser): Boolean =
            transaction.operationType == OperationType.SELL && transaction.firstUser != user
                    || transaction.operationType == OperationType.BUY && transaction.secondUser != user
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

        override fun usersAreNotValidForAction(transaction: Transaction, user: BrokerUser): Boolean =
            transaction.operationType == OperationType.BUY && transaction.firstUser != user
                    || transaction.operationType == OperationType.SELL && transaction.secondUser != user
    };

    abstract fun processWith(
        transaction: Transaction,
        user: BrokerUser,
        latestQuotation: Double,
        broker: Broker,
    ): Transaction

    protected fun validateUsers(transaction: Transaction, user: BrokerUser) {
        if (usersAreNotValidForAction(transaction, user)) {
            throw UnauthorizedUserForAction(user.email, this, transaction.id!!)
        }
    }

    abstract fun usersAreNotValidForAction(
        transaction: Transaction,
        user: BrokerUser
    ): Boolean

}
