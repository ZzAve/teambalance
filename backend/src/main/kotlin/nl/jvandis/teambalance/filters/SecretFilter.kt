package nl.jvandis.teambalance.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

const val SECRET_HEADER = "X-Secret"

/** API paths that are publicly accessible without a secret. */
val PUBLIC_API_PATHS = listOf("/api/settings/season")

@Configuration
class SecretFilter(
    private val secretService: SecretService,
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/api") ||
            PUBLIC_API_PATHS.any { request.requestURI.startsWith(it) }

    override fun destroy() {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            request
                .getHeader(SECRET_HEADER)
                .let { secretService.ensureSecret(it) }

            // call next filter in the filter chain
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, e)
        }
    }
}
