package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.data.NO_ID
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class EventsResponse<T>(
    val totalSize: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val events: List<T>,
)

data class RecurringEventPropertiesRequest(
    val teamBalanceId: String,
    val intervalAmount: Int,
    val intervalTimeUnit: RecurringEventProperties.TimeUnit,
    val amountLimit: Int?,
    val dateLimit: LocalDate?,
    val selectedDays: List<DayOfWeek>,
) {
    init {
        if ((amountLimit == null) == (dateLimit == null)) {
            error(
                "Exactly one of `amountLimit` or `dateLimit` should be provided, " +
                    "was amountLimit=$amountLimit, dateLimit=$dateLimit",
            )
        }

        if (selectedDays.isEmpty()) {
            error("One or more weekdays should be selected for recurring event")
        }
    }

    fun internalize(): RecurringEventProperties? {
        return RecurringEventProperties(
            id = NO_ID,
            teamBalanceId = RecurringEventPropertiesId(teamBalanceId),
            intervalAmount = intervalAmount,
            intervalTimeUnit = intervalTimeUnit,
            amountLimit = amountLimit,
            dateLimit = dateLimit,
            selectedDays = selectedDays.map(DayOfWeek::getValue),
        )
    }
}

data class CreateRecurringEventPropertiesRequest(
    val intervalAmount: Int,
    val intervalTimeUnit: RecurringEventProperties.TimeUnit,
    val amountLimit: Int?,
    val dateLimit: LocalDate?,
    val selectedDays: List<DayOfWeek>,
) {
    init {
        if ((amountLimit == null) == (dateLimit == null)) {
            error(
                "Exactly one of `amountLimit` or `dateLimit` should be provided, " +
                    "was amountLimit=$amountLimit, dateLimit=$dateLimit",
            )
        }

        if (selectedDays.isEmpty()) {
            error("One or more weekdays should be selected for recurring event")
        }
    }

    fun internalize(): RecurringEventProperties? {
        return RecurringEventProperties(
            id = NO_ID,
            teamBalanceId = RecurringEventPropertiesId(UUID.randomUUID().toString()),
            intervalAmount = intervalAmount,
            intervalTimeUnit = intervalTimeUnit,
            amountLimit = amountLimit,
            dateLimit = dateLimit,
            selectedDays = selectedDays.map(DayOfWeek::getValue),
        )
    }
}

data class RecurringEventPropertiesResponse(
    val teamBalanceId: RecurringEventPropertiesId,
    val intervalAmount: Int,
    val intervalTimeUnit: RecurringEventProperties.TimeUnit,
    val amountLimit: Int?,
    val dateLimit: LocalDate?,
    val selectedDays: List<DayOfWeek>,
)

fun RecurringEventProperties.expose() =
    RecurringEventPropertiesResponse(
        teamBalanceId = teamBalanceId,
        intervalAmount = intervalAmount,
        intervalTimeUnit = intervalTimeUnit,
        amountLimit = amountLimit,
        dateLimit = dateLimit,
        selectedDays = selectedDays.map(DayOfWeek::of),
    )
