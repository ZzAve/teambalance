package nl.jvandis.teambalance.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.jvandis.teambalance.filters.DEFAULT_START_OF_SEASON_RAW
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ConfigurationServiceSeasonTest {
    private val repository: ConfigurationRepository = mockk()
    private val objectMapper = ObjectMapper()
    private val service = ConfigurationService(repository, objectMapper)

    @Test
    fun `getStartOfSeason returns DB value when present`() {
        every { repository.getConfig(START_OF_SEASON_CONFIG_KEY) } returns "2024-09-01T00:00:00"

        val result = service.getStartOfSeason()

        assertEquals(LocalDateTime.of(2024, 9, 1, 0, 0, 0), result)
    }

    @Test
    fun `getStartOfSeason returns default fallback when not in DB`() {
        every { repository.getConfig(START_OF_SEASON_CONFIG_KEY) } returns null

        val result = service.getStartOfSeason()

        assertEquals(LocalDateTime.parse(DEFAULT_START_OF_SEASON_RAW), result)
    }

    @Test
    fun `getStartOfSeasonZoned returns DB value as ZonedDateTime in Amsterdam timezone`() {
        every { repository.getConfig(START_OF_SEASON_CONFIG_KEY) } returns "2024-09-01T00:00:00"

        val result = service.getStartOfSeasonZoned()

        assertEquals(LocalDateTime.of(2024, 9, 1, 0, 0, 0).atZone(ZoneId.of("Europe/Amsterdam")), result)
    }

    @Test
    fun `setStartOfSeason persists the value to the repository`() {
        val newStart = LocalDateTime.of(2026, 8, 1, 0, 0, 0)
        every { repository.upsertConfig(START_OF_SEASON_CONFIG_KEY, any()) } returns Unit

        service.setStartOfSeason(newStart)

        verify(exactly = 1) { repository.upsertConfig(START_OF_SEASON_CONFIG_KEY, "2026-08-01T00:00") }
    }
}
