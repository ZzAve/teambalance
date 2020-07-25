package nl.jvandis.teambalance.api

import org.springframework.http.HttpStatus

data class Error(
    val status: HttpStatus,
    val reason: String
)

open class InvalidIdException(val id: Long, val type: String) : RuntimeException()
class InvalidUserException(id: Long) : InvalidIdException(id, "user")
class InvalidTrainingException(id: Long) : InvalidIdException(id, "training")
class InvalidAttendeeException(id: Long) : InvalidIdException(id, "training")

class InvalidSecretException(msg: String) : RuntimeException(msg)

class DataConstraintViolationException(override val message: String) : RuntimeException()
