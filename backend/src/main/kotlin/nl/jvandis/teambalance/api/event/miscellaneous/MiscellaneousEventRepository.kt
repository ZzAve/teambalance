package nl.jvandis.teambalance.api.event.miscellaneous

import nl.jvandis.teambalance.api.match.TeamEventsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MiscellaneousEventRepository : PagingAndSortingRepository<MiscellaneousEvent, Long>, TeamEventsRepository<MiscellaneousEvent> {
    @Query("select e from MiscellaneousEvent e where e.startTime >= :since")
    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable
    ): Page<MiscellaneousEvent>
}
