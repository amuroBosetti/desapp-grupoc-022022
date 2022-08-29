package ar.edu.unq.desapp.grupoc.backenddesappapi.model

class UserRegistry {

    private val registry: MutableList<User> = mutableListOf<User>()

    fun register(user: User) {
        registry.add(user)
    }

    fun findUserWithEmail(email: String): User {
        return registry.first()
    }

}
