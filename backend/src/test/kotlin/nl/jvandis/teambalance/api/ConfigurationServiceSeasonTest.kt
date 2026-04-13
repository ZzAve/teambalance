package nl.jvandis.teambalance.api

import com.fasterxml.jackson.databind.ObjectMapper
import nl.jvandis.teambalance.filters.DEFAULT_START_OF_SEASON_RAW
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.time.ZoneId

class ConfigurationServiceSeasonTest {
    private val repository: ConfigurationRepository = mock()
    private val objectMapper = ObjectMapper()
    private val service = ConfigurationService(repository, objectMapper)

    @Test
    fun `getStartOfSeason returns DB value when present`() {
        `when`(repository.getConfig(START_OF_SEASON_CONFIG_KEY)).thenReturn("2024-09-01T00:00:00")

        val result = service.getStartOfSeason()

        assertEquals(LocalDateTime.of(2024, 9, 1, 0, 0, 0), result)
    }

    @Test
    fun `getStartOfSeason returns default fallback when not in DB`() {
        `when`(repository.getConfig(START_OF_SEASON_CONFIG_KEY)).thenReturn(null)

        val result = service.getStartOfSeason()

        assertEquals(LocalDateTime.parse(DEFAULT_START_OF_SEASON_RAW), result)
    }

    @Test
    fun `getStartOfSeasonZoned returns DB value as ZonedDateTime in Amsterdam timezone`() {
        `when`(repository.getConfig(START_OF_SEASON_CONFIG_KEY)).thenReturn("2024-09-01T00:00:00")

        val result = service.getStartOfSeasonZoned()

        assertEquals(LocalDateTime.of(2024, 9, 1, 0, 0, 0).atZone(ZoneId.of("Europe/Amsterdam")), result)
    }

    @Test
    fun `setStartOfSeason persists the value to the repository`() {
        val newStart = LocalDateTime.of(2026, 8, 1, 0, 0, 0)

        service.setStartOfSeason(newStart)

        verify(repository).upsertConfig(START_OF_SEASON_CONFIG_KEY, "2026-08-01T00:00")
    }

    @Test
    fun `getStartOfSeason throws MalformedConfigFound when DB value is not a valid date`() {
        `when`(repository.getConfig(START_OF_SEASON_CONFIG_KEY)).thenReturn("not-a-date")

        assertThrows<MalformedConfigFound> {
            service.getStartOfSeason()
        }
    }
}
