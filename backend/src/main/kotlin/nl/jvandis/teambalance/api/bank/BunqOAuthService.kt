package nl.jvandis.teambalance.api.bank

import nl.jvandis.teambalance.log
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

/**
 * Service for handling OAuth authentication with Bunq.
 */
@Service
class BunqOAuthService(
    private val bankConfig: BankConfig,
    private val restTemplate: RestTemplate = RestTemplate(),
) {
    companion object {
        private const val BUNQ_OAUTH_AUTHORIZE_URL = "https://oauth.bunq.com/auth"
        private const val BUNQ_OAUTH_TOKEN_URL = "https://api.oauth.bunq.com/v1/token"

        // Store state to prevent CSRF attacks
        private val stateMap = mutableMapOf<String, Long>()

        // Store tokens (in a real application, these should be stored in a database)
        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var tokenExpiresAt: Long = 0
    }

    /**
     * Generates the authorization URL for the OAuth flow.
     *
     * @return The authorization URL to redirect the user to.
     */
    fun getAuthorizationUrl(): String {
        val clientId =
            bankConfig.bunq.oauthClientId
                ?: throw IllegalStateException("OAuth client ID is not configured")
        val redirectUri =
            bankConfig.bunq.oauthRedirectUri
                ?: throw IllegalStateException("OAuth redirect URI is not configured")

        // Generate a random state to prevent CSRF attacks
        val state = UUID.randomUUID().toString()
        stateMap[state] = System.currentTimeMillis()

        // Clean up old states (older than 10 minutes)
        val now = System.currentTimeMillis()
        stateMap.entries.removeIf { (_, timestamp) -> now - timestamp > 10 * 60 * 1000 }

        return UriComponentsBuilder.fromHttpUrl(BUNQ_OAUTH_AUTHORIZE_URL)
            .queryParam("client_id", clientId)
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .build()
            .toUriString()
    }

    /**
     * Handles the OAuth callback by exchanging the authorization code for an access token.
     *
     * @param code The authorization code from the callback.
     * @param state The state from the callback, used to prevent CSRF attacks.
     * @return True if the exchange was successful, false otherwise.
     */
    fun handleCallback(
        code: String,
        state: String,
    ): Boolean {
        // Verify state to prevent CSRF attacks
        if (!stateMap.containsKey(state)) {
            log.warn("Invalid state in OAuth callback: $state")
            return false
        }

        // Remove state from map
        stateMap.remove(state)

        val clientId =
            bankConfig.bunq.oauthClientId
                ?: throw IllegalStateException("OAuth client ID is not configured")
        val clientSecret =
            bankConfig.bunq.oauthClientSecret
                ?: throw IllegalStateException("OAuth client secret is not configured")
        val redirectUri =
            bankConfig.bunq.oauthRedirectUri
                ?: throw IllegalStateException("OAuth redirect URI is not configured")

        // Exchange authorization code for access token
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()
        queryParams.add("grant_type", "authorization_code")
        queryParams.add("code", code)
        queryParams.add("client_id", clientId)
        queryParams.add("client_secret", clientSecret)
        queryParams.add("redirect_uri", redirectUri)

        
        try {
            val response =
                restTemplate.exchange(
                    UriComponentsBuilder.fromHttpUrl(BUNQ_OAUTH_TOKEN_URL)
                        .queryParams(queryParams)
                        .build()
                        .toUri(),
                    HttpMethod.POST,
                    HttpEntity(null,headers),
                    TokenResponse::class.java,
                )

            val tokenResponse = response.body
            if (tokenResponse != null) {
                accessToken = tokenResponse.access_token
//                refreshToken = tokenResponse.refresh_token
//                tokenExpiresAt = System.currentTimeMillis() + (tokenResponse.expires_in * 1000)
                return true
            }
        } catch (e: Exception) {
            log.error("Error exchanging authorization code for access token", e)
        }

        return false
    }

    /**
     * Refreshes the access token using the refresh token.
     *
     * @return True if the refresh was successful, false otherwise.
     */
    fun refreshToken(): Boolean {
        val currentRefreshToken =
            refreshToken
                ?: return false

        val clientId =
            bankConfig.bunq.oauthClientId
                ?: throw IllegalStateException("OAuth client ID is not configured")
        val clientSecret =
            bankConfig.bunq.oauthClientSecret
                ?: throw IllegalStateException("OAuth client secret is not configured")

        // Exchange refresh token for new access token
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("grant_type", "refresh_token")
        body.add("refresh_token", currentRefreshToken)
        body.add("client_id", clientId)
        body.add("client_secret", clientSecret)

        val request = HttpEntity(body, headers)

        try {
            val response =
                restTemplate.exchange(
                    BUNQ_OAUTH_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    TokenResponse::class.java,
                )

            val tokenResponse = response.body
            if (tokenResponse != null) {
                accessToken = tokenResponse.access_token
//                refreshToken = tokenResponse.refresh_token
//                tokenExpiresAt = System.currentTimeMillis() + (tokenResponse.expires_in * 1000)
                return true
            }
        } catch (e: Exception) {
            log.error("Error refreshing access token", e)
        }

        return false
    }

    /**
     * Gets the current access token, refreshing it if necessary.
     *
     * @return The access token, or null if not authenticated.
     */
    fun getAccessToken(): String? {
        // If token is expired or about to expire (within 5 minutes), refresh it
        if (accessToken != null && System.currentTimeMillis() > tokenExpiresAt - (5 * 60 * 1000)) {
            refreshToken()
        }

        return accessToken
    }

    /**
     * Checks if the user is authenticated with Bunq OAuth.
     *
     * @return True if authenticated, false otherwise.
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Clears the authentication tokens.
     */
    fun clearAuthentication() {
        accessToken = null
        refreshToken = null
        tokenExpiresAt = 0
    }

    /**
     * Response object for the token endpoint.
     */
    data class TokenResponse(
        val access_token: String,
        val token_type: String,
//        val expires_in: Int,
//        val refresh_token: String,
    )
}
