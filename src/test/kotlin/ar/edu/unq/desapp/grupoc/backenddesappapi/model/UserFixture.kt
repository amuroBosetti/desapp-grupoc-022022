package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class UserFixture {
    companion object {
        fun aUser(email: String = "alarak@gmail.com", cvu: String = "9506368711100060517136", walletId: String = "12345678"): BrokerUser {
            return BrokerUser(
                email,
                "Alarak",
                "Greycastle",
                "Calle Falsa 123, Beleforth",
                "eluber123",
                cvu,
                walletId
            )
        }

        fun aUserWithReputation(reputationScore: Double): BrokerUser {
            val user = aUser(cvu = "9506368711100060517136", walletId = "12345678")
            user.increaseReputationPoints(reputationScore)
            return user
        }
    }

}
