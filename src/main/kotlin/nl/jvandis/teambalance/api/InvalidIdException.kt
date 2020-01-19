package nl.jvandis.teambalance.api

open class InvalidIdException(val id: Long, val type: String) : RuntimeException()


class InvalidUserException(id: Long) : InvalidIdException(id, "user")
class InvalidTrainingException(id: Long) : InvalidIdException(id, "training")
