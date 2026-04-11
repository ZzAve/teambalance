package nl.jvandis.teambalance.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nl.jvandis.teambalance.api.ConfigurationService
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.time.LocalDateTime
import java.time.ZoneId.of
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Configuration
class DateTimeFilter(
    private val handlerExceptionResolver: HandlerExceptionResolver,
    private val configurationService: ConfigurationService,
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
                    val startOfSeason = configurationService.getStartOfSeason()
                    if (startOfSeason.isAfter(since)) {
                        throw InvalidDateTimeException(
                            "The date $since is not allowed. " +
                                "It's before the start of the season, at $startOfSeason",
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

// Default fallback for the start of the season, used when no config is found in the DB
const val DEFAULT_START_OF_SEASON_RAW: String = "2025-08-01T00:00:00"

fun LocalDateTime.toZonedDateTime(): ZonedDateTime = atZone(of("Europe/Amsterdam"))

class InvalidDateTimeException(
    msg: String,
) : RuntimeException(msg)
