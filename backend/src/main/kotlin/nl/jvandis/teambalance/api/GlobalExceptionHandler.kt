package nl.jvandis.teambalance.api

import jakarta.validation.ConstraintViolationException
import nl.jvandis.teambalance.filters.InvalidDateTimeException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InvalidSecretException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleSecretExceptions(e: InvalidSecretException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                Error(
                    status = HttpStatus.FORBIDDEN,
                    reason = e.message ?: "Forbidden",
                ),
            )

    @ExceptionHandler(InvalidDateTimeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleDateTimeExceptions(e: InvalidDateTimeException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message ?: "Bad request",
                ),
            )

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun constraintException(exception: ConstraintViolationException): ResponseEntity<Error> {
        log.warn("Malformed params", exception)
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    HttpStatus.BAD_REQUEST,
                    exception.constraintViolations
                        .map { it.message }
                        .toString(),
                ),
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadInputArguments(e: MethodArgumentTypeMismatchException): ResponseEntity<Error> {
        log.warn("Invalid request arguments received: ", e)
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "Please verify your input arguments. ${e.message}",
                ),
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingKotlinParameterException(e: HttpMessageNotReadableException): ResponseEntity<Error> {
        log.info("Invalid request arguments received: ", e)
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "Please verify your input arguments. ${e.message}",
                ),
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Error> {
        log.info("Invalid request arguments received: ", e)
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "Please verify your input arguments. ${e.message}",
                ),
            )
    }

    @ExceptionHandler(InvalidIdException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleInvalidRequestArguments(e: InvalidIdException): ResponseEntity<Error> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                Error(
                    status = HttpStatus.NOT_FOUND,
                    reason = "Could not find ${e.type} item with Id ${e.teamBalanceId} ",
                ),
            )
    }

    @ExceptionHandler(DataConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleDataConstraintValidationExceptions(e: DataConstraintViolationException): ResponseEntity<Error> {
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message,
                ),
            )
    }

    @ExceptionHandler(CreateEventException::class)
    fun handleCreateEventException(e: CreateEventException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message,
                ),
            )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = e.message ?: "An illegal argument was provided, somewhere, somehow....",
                ),
            )

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    fun handleAccessDeniedException(e: AccessDeniedException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                Error(
                    status = HttpStatus.FORBIDDEN,
                    reason = "Access to resource is denied.",
                ),
            )

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnhandledExceptions(t: Throwable): ResponseEntity<Error> {
        log.error("Unhandled exception occurred: ${t.message}", t)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                Error(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    reason = "Something went wrong. Please try again later",
                ),
            )
    }
}
