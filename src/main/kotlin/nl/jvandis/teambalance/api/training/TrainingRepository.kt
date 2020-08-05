package nl.jvandis.teambalance.api.training

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TrainingRepository : PagingAndSortingRepository<Training, Long>{
    @Query("select a from Training a where a.startTime >= :startTime")
    fun findAllWithStartTimeAfter(
        @Param("startTime") since: LocalDateTime,
        pageable: Pageable
    ): Page<Training>
}
