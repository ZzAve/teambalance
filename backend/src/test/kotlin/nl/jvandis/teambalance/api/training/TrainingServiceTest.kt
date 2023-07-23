// package nl.jvandis.teambalance.api.training
//
// import nl.jvandis.teambalance.api.database.flyway.AbstractIntegrationTest
// import nl.jvandis.teambalance.api.event.AffectedRecurringEvents
// import nl.jvandis.teambalance.api.event.RecurringEventProperties
// import nl.jvandis.teambalance.api.event.RecurringEventPropertiesRequest
// import nl.jvandis.teambalance.api.event.training.Training
// import nl.jvandis.teambalance.api.event.training.TrainingService
// import nl.jvandis.teambalance.api.event.training.UpdateTrainingRequest
// import nl.wykorijnsburger.kminrandom.KMinRandom
// import nl.wykorijnsburger.kminrandom.minRandom
// import nl.wykorijnsburger.kminrandom.minRandomCached
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.Test
// import org.junit.jupiter.api.extension.ExtendWith
// import org.mockito.ArgumentMatchers
// import org.mockito.junit.jupiter.MockitoExtension
// import org.mockito.kotlin.argWhere
// import org.springframework.beans.factory.annotation.Autowired
// import java.time.DayOfWeek
// import java.time.LocalDateTime
//
// @ExtendWith(MockitoExtension::class)
// class TrainingServiceTest : AbstractIntegrationTest() {
//
//    @Autowired
//    lateinit var service: TrainingService
//
//    init {
//        KMinRandom.supplyValueForClass(LocalDateTime::class, LocalDateTime.now())
//    }
//
//    private val RECURRING_EVENT_PROPERTIES = RecurringEventProperties::class.minRandomCached()
//    private val BASE_DATE_TIME: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0)
//    private val training1 = Training::class.minRandom().copy(
//        id = 1,
//        startTime = BASE_DATE_TIME.minusDays(5),
//        recurringEventProperties = RECURRING_EVENT_PROPERTIES
//    )
//    private val training2 = Training::class.minRandom().copy(
//        id = 2,
//        startTime = BASE_DATE_TIME,
//        recurringEventProperties = RECURRING_EVENT_PROPERTIES
//    )
//    private val training3 = Training::class.minRandom().copy(
//        id = 3,
//        startTime = BASE_DATE_TIME.plusDays(2),
//        recurringEventProperties = RECURRING_EVENT_PROPERTIES
//    )
//    private val defaultCreateRecurringEventPropertiesRequest = RecurringEventPropertiesRequest(
//        teamBalanceId = RECURRING_EVENT_PROPERTIES.teamBalanceId.value,
//        amountLimit = 3,
//        dateLimit = null,
//        intervalAmount = 2,
//        intervalTimeUnit = RecurringEventProperties.TimeUnit.MONTH,
//        selectedDays = listOf(DayOfWeek.FRIDAY)
//    )
//
//    @BeforeEach
//    fun `add training 1,2,3`() {
// //        ctx!!.insertInto(TRAINING)
// //            .values()
//    }
//
//    @Test
//    fun `Should move datetime of all trainings based on the requested training`() {
//        val updateTrainingRequest = UpdateTrainingRequest::class.minRandom().copy(
//            startTime = BASE_DATE_TIME.plusHours(3),
//            recurringEventProperties = defaultCreateRecurringEventPropertiesRequest
// //            RecurringEventUpdateRequest(
// //                id = RECURRING_EVENT_PROPERTIES.teamBalanceId,
// //                type = AffectedRecurringEvents.ALL,
// //                recurringEventProperties = defaultCreateRecurringEventPropertiesRequest
//
//        )
//
// //        whenever(trainingRepository.findByIdOrNull(2L)).thenReturn(training2)
// //        whenever(
// //            trainingRepository.updateAllFromRecurringEvent(
// //                eqValueClass(RECURRING_EVENT_PROPERTIES.teamBalanceId, { it.value }),
// //                any(),
// //                any()
// //            )
// //        ).thenReturn(listOf(training1, training2, training3))
//
//        val updatedTrainings = service.updateTraining(2L, AffectedRecurringEvents.ALL, updateTrainingRequest)
//
// //        verify(trainingRepository).findByIdOrNull(2L)
// //        assertThat(updatedTrainings)
// //            .hasSize(3)
// //            .containsExactly(training1, training2, training3)
//    }
//
// //    @Test
// //    fun `Should set fields to all trainings part of the series`() {
// //        val updateTrainingRequest = UpdateTrainingRequest::class.minRandom().copy(
// //            startTime = BASE_DATE_TIME.plusHours(3),
// //            comment = null,
// //            location = "Some beautiful place",
// //            recurringEvent = RecurringEventUpdateRequest(
// //                id = RECURRING_EVENT_PROPERTIES.teamBalanceId,
// //                type = RecurringEventUpdateType.ALL,
// //                recurringEventProperties = defaultRecurringEventPropertiesRequest
// //            )
// //        )
// //
// //        whenever(trainingRepository.findByIdOrNull(2L)).thenReturn(training2)
// //
// //        val updatedTrainings = service.updateTraining(2L, updateTrainingRequest)
// //
// //        assertThat(updatedTrainings)
// //            .hasSize(3)
// //            .allMatch { it.comment == null }
// //            .allMatch { it.location == "Some beautiful place" }
// //            .allMatch {
// //                it.recurringEventProperties == RECURRING_EVENT_PROPERTIES
// //            }
// //    }
// //
// //    @Test
// //    fun `Should be able to only update current + future events based on flag`() {
// //        val updateTrainingRequest = UpdateTrainingRequest::class.minRandom().copy(
// //            startTime = BASE_DATE_TIME, // unchanged
// //            location = "Location is only updated for current and future events",
// //            recurringEvent = RecurringEventUpdateRequest(
// //                id = RECURRING_EVENT_PROPERTIES.teamBalanceId,
// //                type = RecurringEventUpdateType.CURRENT_AND_FUTURE,
// //                recurringEventProperties = defaultRecurringEventPropertiesRequest
// //            )
// //        )
// //        whenever(trainingRepository.findByIdOrNull(2L)).thenReturn(training2)
// //
// //
// //        val updatedTrainings = service.updateTraining(2L, updateTrainingRequest)
// //
// //        assertThat(updatedTrainings)
// //            .hasSize(2)
// //            .allMatch { it.location == "Location is only updated for current and future events" }
// //            .allMatch {
// //                it.recurringEventProperties == RECURRING_EVENT_PROPERTIES
// //            }
// //
// //        // Training 1 is not part anymore of recurring event, check log?
// //    }
// //
// //    @Test
// //    fun `Should be able to only update current event based on flag`() {
// //        val updateTrainingRequest = UpdateTrainingRequest::class.minRandom().copy(
// //            startTime = BASE_DATE_TIME, // unchanged
// //            location = "Location is only updated for current and future events",
// //            recurringEvent = RecurringEventUpdateRequest(
// //                id = RECURRING_EVENT_PROPERTIES.teamBalanceId,
// //                type = RecurringEventUpdateType.CURRENT,
// //                recurringEventProperties = defaultRecurringEventPropertiesRequest
// //            )
// //        )
// //        whenever(trainingRepository.findByIdOrNull(2L)).thenReturn(training2)
// //
// //
// //        val updatedTrainings = service.updateTraining(2L, updateTrainingRequest)
// //
// //
// //
// //        assertThat(updatedTrainings)
// //            .hasSize(1)
// //            .allMatch { it.location == "Location is only updated for current event" }
// //            .allMatch {
// //                it.recurringEventProperties == RECURRING_EVENT_PROPERTIES
// //            }
// //
// //        // Training 1 is not part anymore of recurring event, check log?
// //    }
// //
// //    @Test
// //    fun `Should be able to modify recurrence parameters`() {
// //        val updateTrainingRequest = UpdateTrainingRequest::class.minRandom().copy(
// //            startTime = BASE_DATE_TIME, // unchanged
// //            location = "Location is only updated for current and future events",
// //            recurringEvent = RecurringEventUpdateRequest(
// //                id = RECURRING_EVENT_PROPERTIES.teamBalanceId,
// //                type = RecurringEventUpdateType.CURRENT_AND_FUTURE,
// //                recurringEventProperties = defaultRecurringEventPropertiesRequest.copy(intervalAmount = 17)
// //            )
// //        )
// //        whenever(trainingRepository.findByIdOrNull(2L)).thenReturn(training2)
// //
// //
// //        val updatedTrainings = service.updateTraining(2L, updateTrainingRequest)
// //
// //        assertThat(updatedTrainings)
// //            .hasSize(1)
// //            .allMatch { it.location == "Location is only updated for current event" }
// //            .allMatch {
// //                it.recurringEventProperties == RECURRING_EVENT_PROPERTIES
// //            }
// //
// //        // Training 1 is not part anymore of recurring event, check log?
// //    }
//
//    companion object {
//    }
// }
//
// object MockitoHelper {
//
//    /**
//     * Workaround from https://github.com/mockito/mockito-kotlin/issues/445#issuecomment-983619131
//     */
//    inline fun <Outer, reified Inner> eqValueClass(
//        expected: Outer,
//        crossinline access: (Outer) -> Inner,
//        test: ((actual: Any) -> Boolean) -> Any? = ::argWhere
//    ): Outer {
//        val assertion: (Any) -> Boolean = { actual ->
//            if (actual is Inner) {
//                access(expected) == actual
//            } else {
//                expected == actual
//            }
//        }
//        @Suppress("UNCHECKED_CAST")
//        return test(assertion) as Outer? ?: expected
//    }
//
//    /**
//     * Workaround for value classes: https://github.com/mockito/mockito-kotlin/issues/445#issuecomment-1177218590
//     */
//    inline fun <reified T> anyValueClass(): T =
//        ArgumentMatchers.any(T::class.java)
//            ?: T::class.java.getDeclaredMethod("box-impl", T::class.java.declaredFields.first().type)
//                .invoke(null, null) as T
// }
