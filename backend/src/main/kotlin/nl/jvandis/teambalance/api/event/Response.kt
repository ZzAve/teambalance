package nl.jvandis.teambalance.api.event

import nl.jvandis.teambalance.TeamBalanceId
import nl.jvandis.teambalance.data.NO_ID
import java.time.DayOfWeek
import java.time.LocalDate

data class EventsResponse<T>(
    val totalSize: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val events: List<T>,
)

data class RecurringEventPropertiesRequest(
    val id: String,
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

    fun internalize() =
        RecurringEventProperties(
            id = NO_ID,
            teamBalanceId = TeamBalanceId(id),
            intervalAmount = intervalAmount,
            intervalTimeUnit = intervalTimeUnit,
            amountLimit = amountLimit,
            dateLimit = dateLimit,
            selectedDays = selectedDays.map(DayOfWeek::getValue),
        )
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

    fun internalize() =
        RecurringEventProperties(
            id = NO_ID,
            teamBalanceId = TeamBalanceId.random(),
            intervalAmount = intervalAmount,
            intervalTimeUnit = intervalTimeUnit,
            amountLimit = amountLimit,
            dateLimit = dateLimit,
            selectedDays = selectedDays.map(DayOfWeek::getValue),
        )
}

data class RecurringEventPropertiesResponse(
    val id: String,
    val intervalAmount: Int,
    val intervalTimeUnit: RecurringEventProperties.TimeUnit,
    val amountLimit: Int?,
    val dateLimit: LocalDate?,
    val selectedDays: List<DayOfWeek>,
)

fun RecurringEventProperties.expose() =
    RecurringEventPropertiesResponse(
        id = teamBalanceId.value,
        intervalAmount = intervalAmount,
        intervalTimeUnit = intervalTimeUnit,
        amountLimit = amountLimit,
        dateLimit = dateLimit,
        selectedDays = selectedDays.map(DayOfWeek::of),
    )

data class UserAddRequest(
    val userId: String,
)
