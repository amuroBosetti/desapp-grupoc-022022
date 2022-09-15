package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class UserFixture {
    companion object {
        fun aUser(email: String = "alarak@gmail.com"): User {
            return User(
                email,
                "Alarak",
                "Greycastle",
                "Calle Falsa 123, Beleforth",
                "eluber123",
                "9506368711100060517136",
                "12345678"
            )
        }

        fun aUserWithReputation(reputationScore: Double): User {
            val user = aUser()
            user.increaseReputationPoints(reputationScore)
            return user
        }
    }

}
