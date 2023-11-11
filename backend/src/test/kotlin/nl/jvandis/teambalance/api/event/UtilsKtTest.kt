package nl.jvandis.teambalance.api.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Period
import java.util.stream.Stream

class UtilsKtTest {
    @ParameterizedTest
    @MethodSource("expectedNextDates")
    fun `Should return expectedNextDate for the given inputs`(
        previousDateTime: LocalDateTime,
        interval: Period,
        daysOfWeek: List<DayOfWeek>,
        expectedNextDate: LocalDateTime,
    ) {
        // Selecting Friday
        assertEquals(
            expectedNextDate,
            nextEventDate(
                previousDateTime,
                interval,
                daysOfWeek,
            ),
        )
    }

    @Test
    fun `Should throw an exception if start date is not part of the recurring event sequence`() {
        // Selecting Friday
        assertThrows(IllegalArgumentException::class.java) {
            CreateRecurringEventPropertiesRequest(
                1,
                RecurringEventProperties.TimeUnit.WEEK,
                10,
                null,
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.FRIDAY,
                ),
            ).getRecurringEventDates(
                GOOD_FRIDAY.minusDays(1),
            )
        }
    }

    companion object {
        val GOOD_FRIDAY: LocalDateTime = LocalDateTime.of(2023, 4, 7, 0, 0, 0)

        @JvmStatic
        fun expectedNextDates(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    GOOD_FRIDAY,
                    Period.ofWeeks(1),
                    listOf(DayOfWeek.FRIDAY),
                    GOOD_FRIDAY.plusDays(7),
                ),
                arguments(
                    GOOD_FRIDAY,
                    Period.ofWeeks(3),
                    listOf(DayOfWeek.FRIDAY),
                    GOOD_FRIDAY.plusDays(21),
                ),
                arguments(
                    GOOD_FRIDAY.minusDays(4),
                    Period.ofWeeks(1),
                    listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                    GOOD_FRIDAY,
                ),
                arguments(
                    GOOD_FRIDAY,
                    Period.ofWeeks(1),
                    listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                    GOOD_FRIDAY.plusDays(3),
                ),
                arguments(
                    GOOD_FRIDAY,
                    Period.ofWeeks(3),
                    listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                    GOOD_FRIDAY.plusDays(3).plusDays(14),
                ),
                arguments(
                    GOOD_FRIDAY.minusDays(1).minusDays(7),
                    Period.ofMonths(3),
                    listOf(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY),
                    GOOD_FRIDAY.minusDays(2).minusDays(7).plusMonths(3),
                ),
            )
        }
    }
}
