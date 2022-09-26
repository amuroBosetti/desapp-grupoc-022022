package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class UserRegistry {

    private val registry: MutableList<BrokerUser> = mutableListOf()

    fun register(user: BrokerUser) {
        registry.add(user)
    }

    fun findUserWithEmail(email: String): BrokerUser {
        return registry.first()
    }

}
