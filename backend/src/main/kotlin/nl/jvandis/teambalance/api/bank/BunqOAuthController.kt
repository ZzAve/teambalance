package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.log
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

/**
 * Controller for handling Bunq OAuth operations.
 */
@RestController
@RequestMapping("/api/bank/bunq/oauth")
class BunqOAuthController(
    private val bunqOAuthService: BunqOAuthService,
) {
    /**
     * Initiates the OAuth flow by redirecting to the Bunq authorization page.
     *
     * @return A redirect to the Bunq authorization page.
     */
    @GetMapping("/authorize")
    fun authorize(): RedirectView {
        val authorizationUrl = bunqOAuthService.getAuthorizationUrl()
        log.info("Redirecting to Bunq authorization URL: $authorizationUrl")
        return RedirectView(authorizationUrl)
    }

    /**
     * Handles the OAuth callback from Bunq.
     *
     * @param code The authorization code from Bunq.
     * @param state The state parameter to prevent CSRF attacks.
     * @return A redirect to the frontend with success or error status.
     */
    @GetMapping("/callback")
    fun callback(
        @RequestParam("code") code: String,
        @RequestParam("state") state: String,
    ): RedirectView {
        log.info("Received OAuth callback with code: $code and state: $state")

        val success = bunqOAuthService.handleCallback(code, state)

        // Redirect back to the frontend
        return if (success) {
            RedirectView("/connect-bunq?success=true")
        } else {
            RedirectView("/connect-bunq?success=false")
        }
    }

    /**
     * Gets the current OAuth status.
     *
     * @return The OAuth status.
     */
    @GetMapping("/status")
    fun getStatus(): OAuthStatusResponse {
        val isAuthenticated = bunqOAuthService.isAuthenticated()
        return OAuthStatusResponse(isAuthenticated)
    }

    /**
     * Clears the OAuth authentication.
     *
     * @return A success response.
     */
    @PostMapping("/clear")
    fun clearAuthentication(): ResponseEntity<Unit> {
        bunqOAuthService.clearAuthentication()
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    /**
     * Response object for the status endpoint.
     */
    data class OAuthStatusResponse(
        val authenticated: Boolean,
    )
}
