package ar.edu.unq.desapp.grupoc.backenddesappapi.exception

class NotRegisteredUserException(user: String) : RuntimeException("User with email $user is not registered")
