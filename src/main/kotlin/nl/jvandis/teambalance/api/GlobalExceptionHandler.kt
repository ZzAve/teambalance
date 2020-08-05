package nl.jvandis.teambalance.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.validation.ConstraintViolationException

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InvalidSecretException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleSecretExceptions(e: InvalidSecretException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                Error(
                    status = HttpStatus.FORBIDDEN,
                    reason = e.message ?: "Forbidden"
                )
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
                        .toString()
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadInputArguments(e: MethodArgumentTypeMismatchException): ResponseEntity<Error> {
        log.info("Invalid request arguments received: ", e)
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "Please verify your input arguments"
                )
            )
    }

    @ExceptionHandler(InvalidIdException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidRequestArguments(e: InvalidIdException): ResponseEntity<Error> {
        return ResponseEntity
            .badRequest()
            .body(
                Error(
                    status = HttpStatus.BAD_REQUEST,
                    reason = "Could not apply operation because Id was invalid (${e.type} id ${e.id}) "
                )
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
                    reason = e.message
                )
            )
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnhandledExceptions(t: Throwable): ResponseEntity<Error> {
        log.error("Unhandled exception occured: ${t.message}", t)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                Error(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    reason = "Something went wrong. Please try again later"
                )
            )
    }
}
