package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class UserFixture {
    companion object {
        fun aUser(
            email: String = "alarak@gmail.com",
            cvu: String = "9506368711100060517136",
            walletId: String = "12345678",
            userId: Long? = null,
            password: String = "eluber123"
        ): BrokerUser {
            val brokerUser = BrokerUser(
                email,
                "Alarak",
                "Greycastle",
                "Calle Falsa 123, Beleforth",
                password,
                cvu,
                walletId
            )
            brokerUser.id = userId
            return brokerUser
        }

        fun aUserWithReputation(reputationScore: Double): BrokerUser {
            val user = aUser(cvu = "9506368711100060517136", walletId = "12345678", userId = 5L, password = "eluber123")
            user.increaseReputationPoints(reputationScore)
            return user
        }
    }

}
