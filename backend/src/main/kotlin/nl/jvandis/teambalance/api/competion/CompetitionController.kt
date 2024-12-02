package nl.jvandis.teambalance.api.competion

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "competition")
@RestController
@RequestMapping(path = ["/api/competition"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CompetitionController(private val service: CompetitionService) {
    @GetMapping
    fun getRanking(): CompetitionRankingDto = service.getRanking().expose()
}

fun CompetitionRanking.expose() =
    CompetitionRankingDto(
        webUrl = webUrl,
        lastUpdateTimestamp = lastUpdateTimestamp,
        entries =
            entries.map {
                RankEntryDto(
                    number = it.number,
                    team = it.team,
                    matches = it.matches,
                    points = it.points,
                    setsFor = it.setsFor,
                    setsAgainst = it.setsAgainst,
                    pointsFor = it.pointsFor,
                    pointsAgainst = it.pointsAgainst,
                )
            },
    )
