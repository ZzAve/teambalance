// package nl.jvandis.teambalance.api.training
//
// import io.micronaut.data.annotation.Repository
// import nl.jvandis.teambalance.api.match.TeamEventsRepository
// import java.time.LocalDateTime
//
// @Repository
// interface TrainingRepository : PagingAndSortingRepository<Training, Long>, TeamEventsRepository<Training> {
//     @Query("select t from Training t where t.startTime >= :since")
//     override fun findAllWithStartTimeAfter(
//         since: LocalDateTime,
//         pageable: Pageable
//     ): Page<Training>
// }
