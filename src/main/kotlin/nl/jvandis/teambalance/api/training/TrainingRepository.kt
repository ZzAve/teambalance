package nl.jvandis.teambalance.api.training

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface TrainingRepository : PagingAndSortingRepository<Training, Long>
