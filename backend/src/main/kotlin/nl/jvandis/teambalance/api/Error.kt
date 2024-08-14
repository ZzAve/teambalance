package nl.jvandis.teambalance.api

import nl.jvandis.teambalance.TeamBalanceId
import org.springframework.http.HttpStatus

data class Error(
    val status: HttpStatus,
    val reason: String,
)

open class InvalidIdException(val teamBalanceId: TeamBalanceId, val type: String) : RuntimeException()

class InvalidUserException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "user")

class InvalidTransactionException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "transaction")

class InvalidAliasException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "alias")

class InvalidTrainingException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "training")

class InvalidMatchException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "match")

class InvalidMiscellaneousEventException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "miscellaneous-event")

class InvalidEventException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "event")

class InvalidAttendeeException(teamBalanceId: TeamBalanceId) : InvalidIdException(teamBalanceId, "attendee")

class InvalidSecretException(msg: String) : RuntimeException(msg)

class DataConstraintViolationException(override val message: String) : RuntimeException()

class CreateEventException(override val message: String) : RuntimeException()
