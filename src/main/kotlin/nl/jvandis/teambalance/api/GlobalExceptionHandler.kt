package nl.jvandis.teambalance.api

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import org.slf4j.LoggerFactory
import javax.inject.Singleton

private val log = LoggerFactory.getLogger("GlobalExceptionHandler")
//
// @ControllerAdvice
// class GlobalExceptionHandler {
//
//     @ExceptionHandler(InvalidSecretException::class)
//     @ResponseStatus(HttpStatus.FORBIDDEN)
//     fun handleSecretExceptions(e: InvalidSecretException) =
//         ResponseEntity.status(HttpStatus.FORBIDDEN)
//             .body(
//                 Error(
//                     status = HttpStatus.FORBIDDEN,
//                     reason = e.message ?: "Forbidden"
//                 )
//             )
//
//     @ExceptionHandler(ConstraintViolationException::class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     fun constraintException(exception: ConstraintViolationException): ResponseEntity<Error> {
//         log.warn("Malformed params", exception)
//         return ResponseEntity
//             .badRequest()
//             .body(
//                 Error(
//                     HttpStatus.BAD_REQUEST,
//                     exception.constraintViolations
//                         .map { it.message }
//                         .toString()
//                 )
//             )
//     }
//
//     @ExceptionHandler(MethodArgumentTypeMismatchException::class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     fun handleBadInputArguments(e: MethodArgumentTypeMismatchException): ResponseEntity<Error> {
//         log.info("Invalid request arguments received: ", e)
//         return ResponseEntity
//             .badRequest()
//             .body(
//                 Error(
//                     status = HttpStatus.BAD_REQUEST,
//                     reason = "Please verify your input arguments"
//                 )
//             )
//     }
//
//     @ExceptionHandler(HttpMessageNotReadableException::class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     fun handleMissingKotlinParameterException(e: HttpMessageNotReadableException): ResponseEntity<Error> {
//         log.info("Invalid request arguments received: ", e)
//         return ResponseEntity
//             .badRequest()
//             .body(
//                 Error(
//                     status = HttpStatus.BAD_REQUEST,
//                     reason = "Please verify your input arguments"
//                 )
//             )
//     }
//
//     @ExceptionHandler(MethodArgumentNotValidException::class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Error> {
//         log.info("Invalid request arguments received: ", e)
//         return ResponseEntity
//             .badRequest()
//             .body(
//                 Error(
//                     status = HttpStatus.BAD_REQUEST,
//                     reason = "Please verify your input arguments"
//                 )
//             )
//     }
//
//     @ExceptionHandler(InvalidIdException::class)
//     @ResponseStatus(HttpStatus.NOT_FOUND)
//     fun handleInvalidRequestArguments(e: InvalidIdException): ResponseEntity<Error> {
//         return ResponseEntity
//             .status(HttpStatus.NOT_FOUND)
//             .body(
//                 Error(
//                     status = HttpStatus.NOT_FOUND,
//                     reason = "Could not find ${e.type} item with Id ${e.id} "
//                 )
//             )
//     }
//
//     @ExceptionHandler(DataConstraintViolationException::class)
//     @ResponseStatus(HttpStatus.BAD_REQUEST)
//     fun handleDataConstraintValidationExceptions(e: DataConstraintViolationException): ResponseEntity<Error> {
//         return ResponseEntity
//             .badRequest()
//             .body(
//                 Error(
//                     status = HttpStatus.BAD_REQUEST,
//                     reason = e.message
//                 )
//             )
//     }
// }

@Produces
@Singleton
@Requires(classes = [Throwable::class, ExceptionHandler::class])
class UnhandledExceptionHandler : ExceptionHandler<Throwable, HttpResponse<ErrorResponse>> {
    override fun handle(request: HttpRequest<*>?, exception: Throwable?): HttpResponse<ErrorResponse> {
        log.error("Unhandled exception", exception)
        return HttpResponse
            .status<ErrorResponse>(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR,
                    reason = "Something went wrong. Please try again later"
                )
            )
    }
}
