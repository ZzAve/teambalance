package nl.jvandis.teambalance.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class DateTimeFilter(
    private val eventService: DateTimeBoundService,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    override fun destroy() {}
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            request.getParameter("since")
                ?.let { eventService.ensureDateTimeLimit(LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)) }

            // call next filter in the filter chain
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, e)
        }
    }
}
