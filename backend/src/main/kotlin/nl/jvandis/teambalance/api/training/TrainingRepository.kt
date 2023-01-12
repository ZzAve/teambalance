package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.api.match.TeamEventsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TrainingRepository : PagingAndSortingRepository<Training, Long>, TeamEventsRepository<Training> {
    @Query("select t from Training t where t.startTime >= :since")
    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable
    ): Page<Training>
}
