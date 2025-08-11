package nl.jvandis.teambalance.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nl.jvandis.teambalance.api.bank.EUROPE_AMSTERDAM
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Configuration
class DateTimeFilter(
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            request
                .getParameter("since")
                ?.also {
                    val since = LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    if (START_OF_SEASON.isAfter(since)) {
                        throw InvalidDateTimeException(
                            "The date $since is not allowed. " +
                                "It's before the start of the season, at $START_OF_SEASON",
                        )
                    }
                }

            // call next filter in the filter chain
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, e)
        }
    }
}

// Limit is bound to the start of the season, which typically starts around the 10th of August
const val START_OF_SEASON_RAW: String = "2025-08-01T00:00:00"
val START_OF_SEASON: LocalDateTime = LocalDateTime.parse(START_OF_SEASON_RAW)
val START_OF_SEASON_ZONED: ZonedDateTime = START_OF_SEASON.toZonedDateTime()

fun LocalDateTime.toZonedDateTime(): ZonedDateTime = atZone(EUROPE_AMSTERDAM)

class InvalidDateTimeException(
    msg: String,
) : RuntimeException(msg)
