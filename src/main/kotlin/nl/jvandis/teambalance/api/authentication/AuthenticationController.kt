package nl.jvandis.teambalance.api.authentication

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.ErrorResponse
import nl.jvandis.teambalance.api.InvalidSecretException
import nl.jvandis.teambalance.api.SECRET_HEADER
import nl.jvandis.teambalance.api.SecretService

@Tag( name= "authentication")
@Controller(value = "api/authentication", produces = [MediaType.APPLICATION_JSON])
class AuthenticationController(
    private val secretService: SecretService
) {

    @Get
    fun authenticate(
        @Header(value = SECRET_HEADER) secret: String?
    ): Success {
        secretService.ensureSecret(secret)
        return Success()
    }

    data class Success(
        val message: String = "You have managed to get access. Well done"
    )

    @Error(InvalidSecretException::class)
    fun handleSecretExceptions(request: HttpRequest<*>, e: InvalidSecretException): HttpResponse<ErrorResponse> =
        HttpResponse.status<ErrorResponse>(HttpStatus.UNAUTHORIZED, "Check your credentials")
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED,
                    reason = e.message ?: "Unauthorized"
                )
            )
}
