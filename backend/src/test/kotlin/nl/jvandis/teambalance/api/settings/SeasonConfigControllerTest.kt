package nl.jvandis.teambalance.api.settings

import nl.jvandis.teambalance.api.ConfigurationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime

class SeasonConfigControllerTest {
    private val configurationService: ConfigurationService = mock()
    private val controller = SeasonConfigController(configurationService)

    @Test
    fun `GET season config returns current start of season`() {
        val expectedDate = LocalDateTime.of(2025, 8, 1, 0, 0, 0)
        `when`(configurationService.getStartOfSeason()).thenReturn(expectedDate)

        val result = controller.getSeasonConfig()

        assertEquals(SeasonConfigResponse(startOfSeason = expectedDate), result)
    }

    @Test
    fun `PUT season config sets start of season to 2025-08-02 and returns updated value`() {
        val newDate = LocalDateTime.of(2025, 8, 2, 0, 0, 0)
        `when`(configurationService.getStartOfSeason()).thenReturn(newDate)

        val result = controller.updateSeasonConfig(UpdateSeasonConfigRequest(startOfSeason = newDate))

        verify(configurationService).setStartOfSeason(newDate)
        assertEquals(SeasonConfigResponse(startOfSeason = newDate), result)
    }
}
