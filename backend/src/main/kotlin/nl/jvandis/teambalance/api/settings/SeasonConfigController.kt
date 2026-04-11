package nl.jvandis.teambalance.api.settings

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.tags.Tag
import nl.jvandis.teambalance.api.Admin
import nl.jvandis.teambalance.api.ConfigurationService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@Tag(name = "config")
@RequestMapping(path = ["/api/config"], produces = [MediaType.APPLICATION_JSON_VALUE])
class SeasonConfigController(
    private val configurationService: ConfigurationService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/season")
    fun getSeasonConfig(): SeasonConfigResponse {
        log.debug("getSeasonConfig")
        return SeasonConfigResponse(startOfSeason = configurationService.getStartOfSeason())
    }

    @Admin
    @PutMapping("/season")
    fun updateSeasonConfig(
        @RequestBody request: UpdateSeasonConfigRequest,
    ): SeasonConfigResponse {
        log.info("Updating startOfSeason to ${request.startOfSeason}")
        configurationService.setStartOfSeason(request.startOfSeason)
        return SeasonConfigResponse(startOfSeason = configurationService.getStartOfSeason())
    }
}

data class SeasonConfigResponse(
    val startOfSeason: LocalDateTime,
)

data class UpdateSeasonConfigRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startOfSeason: LocalDateTime,
)
