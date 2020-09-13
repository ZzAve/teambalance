package nl.jvandis.teambalance.api

import io.micronaut.http.HttpStatus

data class ErrorResponse(
    val status: HttpStatus,
    val reason: String
)

open class InvalidIdException(val id: Long, val type: String) : RuntimeException()
class InvalidUserException(id: Long) : InvalidIdException(id, "user")
class InvalidTrainingException(id: Long) : InvalidIdException(id, "training")
class InvalidMatchException(id: Long) : InvalidIdException(id, "match")
class InvalidAttendeeException(id: Long) : InvalidIdException(id, "attendee")

class InvalidSecretException(msg: String) : RuntimeException(msg)

class DataConstraintViolationException(override val message: String) : RuntimeException()
