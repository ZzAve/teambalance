package nl.jvandis.teambalance.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nl.jvandis.teambalance.api.ConfigurationService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.HandlerExceptionResolver
import java.time.LocalDateTime

class DateTimeFilterTest {
    private val handlerExceptionResolver: HandlerExceptionResolver = mock()
    private val configurationService: ConfigurationService = mock()
    private val filter = DateTimeFilter(handlerExceptionResolver, configurationService)

    @Test
    fun `passes request through when no since parameter is present`() {
        val request: HttpServletRequest = mock()
        val response: HttpServletResponse = mock()
        val chain: FilterChain = mock()
        whenever(request.getParameter("since")).thenReturn(null)

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verify(handlerExceptionResolver, never()).resolveException(any(), any(), any(), any())
    }

    @Test
    fun `passes request through when since is after start of season`() {
        val startOfSeason = LocalDateTime.of(2025, 8, 1, 0, 0, 0)
        whenever(configurationService.getStartOfSeason()).thenReturn(startOfSeason)
        val request: HttpServletRequest = mock()
        val response: HttpServletResponse = mock()
        val chain: FilterChain = mock()
        whenever(request.getParameter("since")).thenReturn("2025-09-01T00:00:00")

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verify(handlerExceptionResolver, never()).resolveException(any(), any(), any(), any())
    }

    @Test
    fun `passes request through when since equals start of season`() {
        val startOfSeason = LocalDateTime.of(2025, 8, 1, 0, 0, 0)
        whenever(configurationService.getStartOfSeason()).thenReturn(startOfSeason)
        val request: HttpServletRequest = mock()
        val response: HttpServletResponse = mock()
        val chain: FilterChain = mock()
        whenever(request.getParameter("since")).thenReturn("2025-08-01T00:00:00")

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        verify(handlerExceptionResolver, never()).resolveException(any(), any(), any(), any())
    }

    @Test
    fun `resolves exception when since is before start of season`() {
        val startOfSeason = LocalDateTime.of(2025, 8, 1, 0, 0, 0)
        whenever(configurationService.getStartOfSeason()).thenReturn(startOfSeason)
        val request: HttpServletRequest = mock()
        val response: HttpServletResponse = mock()
        val chain: FilterChain = mock()
        whenever(request.getParameter("since")).thenReturn("2025-07-01T00:00:00")

        filter.doFilter(request, response, chain)

        verify(handlerExceptionResolver).resolveException(
            eq(request),
            eq(response),
            isNull(),
            any<InvalidDateTimeException>(),
        )
        verify(chain, never()).doFilter(any(), any())
    }

    @Test
    fun `resolves exception when since is well before start of season`() {
        val startOfSeason = LocalDateTime.of(2025, 8, 1, 0, 0, 0)
        whenever(configurationService.getStartOfSeason()).thenReturn(startOfSeason)
        val request: HttpServletRequest = mock()
        val response: HttpServletResponse = mock()
        val chain: FilterChain = mock()
        whenever(request.getParameter("since")).thenReturn("2020-01-01T00:00:00")

        filter.doFilter(request, response, chain)

        verify(handlerExceptionResolver).resolveException(
            eq(request),
            eq(response),
            isNull(),
            any<InvalidDateTimeException>(),
        )
        verify(chain, never()).doFilter(any(), any())
    }
}
