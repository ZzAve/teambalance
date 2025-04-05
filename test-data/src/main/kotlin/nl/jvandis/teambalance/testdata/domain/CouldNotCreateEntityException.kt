package nl.jvandis.teambalance.testdata.domain

sealed class CouldNotCreateEntityException(
    override val message: String?,
    override val cause: Throwable? = null,
) : RuntimeException() {
    class UserCreationException(
        message: String?,
        cause: Throwable? = null,
    ) : CouldNotCreateEntityException(message, cause)

    class AttendeeCreationException(
        message: String?,
        cause: Throwable? = null,
    ) : CouldNotCreateEntityException(message, cause)

    class AliasCreationException(
        message: String?,
        cause: Throwable? = null,
    ) : CouldNotCreateEntityException(message, cause)

    class TransactionExclusionCreationException(
        message: String?,
        cause: Throwable? = null,
    ) : CouldNotCreateEntityException(message, cause)

    class EventCreationException(
        message: String?,
        cause: Throwable? = null,
    ) : CouldNotCreateEntityException(message, cause)
}
