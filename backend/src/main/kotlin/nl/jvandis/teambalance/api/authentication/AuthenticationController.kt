package nl.jvandis.teambalance.api.authentication

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.Error
import nl.jvandis.teambalance.api.InvalidSecretException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "authentication")
@RequestMapping(path = ["api/authentication"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthenticationController() {
    @GetMapping
    fun authenticate(): Success {
        return Success()
    }

    data class Success(
        val message: String = "You have managed to get access. Well done",
    )

    @ExceptionHandler(InvalidSecretException::class)
    fun handleSecretExceptions(e: InvalidSecretException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                Error(
                    status = HttpStatus.UNAUTHORIZED,
                    reason = e.message ?: "Unauthorized",
                ),
            )
}
