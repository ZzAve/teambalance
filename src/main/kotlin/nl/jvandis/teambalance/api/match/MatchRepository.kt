package nl.jvandis.teambalance.api.match

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MatchRepository : PagingAndSortingRepository<Match, Long>, TeamEventsRepository<Match> {
    @Query("select m from Match m where m.startTime >= :since")
    override fun findAllWithStartTimeAfter(
        since: LocalDateTime,
        pageable: Pageable
    ): Page<Match>
}
