package nl.jvandis.teambalance.filters

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(javaClass)
    }
}
