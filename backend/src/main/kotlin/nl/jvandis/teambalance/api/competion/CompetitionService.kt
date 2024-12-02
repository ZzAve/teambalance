package nl.jvandis.teambalance.api.competion

import nl.jvandis.teambalance.api.ConfigurationService
import org.springframework.stereotype.Service

data class CompetitionConfig(
    val regionId: String,
    val competitionId: String,
    val poolId: String,
)

@Service
class CompetitionService(
    private val nevoboClient: NevoboClient,
    private val configurationService: ConfigurationService,
) {
    fun getRanking(): CompetitionRanking {
        val config = configurationService.getConfig("competitionConfig", CompetitionConfig::class)
        return nevoboClient.getRankingForCompetition(config)
        // "regio-west",
        // "competitie-seniorencompetitie-1",
        // "regio-west-h2e-10",
    }
}
