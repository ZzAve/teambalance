package nl.jvandis.teambalance.api.training

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingRepository : CrudRepository<Training, Long>