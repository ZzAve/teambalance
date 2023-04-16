package nl.jvandis.teambalance.api.event

import java.time.LocalDate

enum class Place {
    HOME,
    AWAY
}

data class RecurringEventPropertiesRequest(
    val intervalAmount: Int,
    val intervalTimeUnit: TimeUnit,
    val amountLimit: Int?,
    val dateLimit: LocalDate?,
    val selectedDays: List<Day>

) {
    enum class TimeUnit {
        WEEK,
        MONTH
    }

    enum class Day {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }

    init {
        if ((amountLimit == null) == (dateLimit == null)) {
            error("Exactly one of `amountLimit` or `dateLimit` should be provided")
        }

        if (selectedDays.isEmpty()) {
            error("One or more weekdays should be selected for recurring event")
        }
    }
}
