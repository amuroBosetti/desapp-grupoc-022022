package ar.edu.unq.desapp.grupoc.backenddesappapi.repository

import ar.edu.unq.desapp.grupoc.backenddesappapi.model.BrokerUser
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
@Transactional
interface UserRepository : CrudRepository<BrokerUser, Long>