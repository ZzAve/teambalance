package nl.jvandis.teambalance.api.settings

import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.filters.START_OF_SEASON_RAW
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "settings")
@RequestMapping(path = ["api/settings"], produces = [MediaType.APPLICATION_JSON_VALUE])
class SettingsController {
    @GetMapping("/season")
    fun getSeason(): SeasonResponse = SeasonResponse(startOfSeason = START_OF_SEASON_RAW)

    data class SeasonResponse(
        val startOfSeason: String,
    )
}
