package nl.jvandis.teambalance.filters

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val SECRET_HEADER = "X-Secret"

@Configuration
class SecretFilter(
    private val secretService: SecretService,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/api")
    }

    override fun destroy() {}
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            request.getHeader(SECRET_HEADER)
                .let { secretService.ensureSecret(it) }

            // call next filter in the filter chain
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, e)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(javaClass)
    }
}
