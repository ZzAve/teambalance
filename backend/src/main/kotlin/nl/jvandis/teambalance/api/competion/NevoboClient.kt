package nl.jvandis.teambalance.api.competion

import com.fasterxml.jackson.databind.ObjectMapper
import nl.jvandis.teambalance.api.CacheConfig
import nl.jvandis.teambalance.api.setupCache
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

data class RssFeed(
    val channel: Channel,
)

data class Channel(
    val title: String,
    val link: String,
    val description: String,
    val lastBuildDate: String,
    val item: List<Item>,
    val ranking: List<Ranking>,
)

data class Item(
    val title: String,
    val link: String,
    val guid: String,
    val description: String,
    val pubDate: String,
)

/**
 * <stand:ranking>
 *
 *             <stand:nummer>1</stand:nummer>
 *             <stand:team id="3261HS 4"><![CDATA[Gemini S HS 4]]></stand:team>
 *             <stand:wedstrijden>7</stand:wedstrijden>
 *             <stand:punten>31</stand:punten>
 *             <stand:setsvoor>25</stand:setsvoor>
 *             <stand:setstegen>3</stand:setstegen>
 *             <stand:puntenvoor>699</stand:puntenvoor>
 *             <stand:puntentegen>504</stand:puntentegen>
 *         </stand:ranking>
 */
data class Ranking(
    val nummer: String,
    val team: String,
    val wedstrijden: String,
    val punten: String,
    val setsvoor: String,
    val setstegen: String,
    val puntenvoor: String,
    val puntentegen: String,
)

@ConfigurationProperties("app.competition")
data class CompetitionProperties(
    val cacheConfig: CacheConfig,
)

@Component
class NevoboClient(
    private val restTemplate: RestTemplate,
    @Qualifier("xmlMapper")
    private val xmlMapper: ObjectMapper,
    competitionProperties: CompetitionProperties,
) {
    private val rankingCache =
        setupCache<CompetitionConfig, CompetitionRanking>(competitionProperties.cacheConfig) {
            updateRankingForCompetition(
                regionId = it.regionId,
                competitionId = it.competitionId,
                poolId = it.poolId,
            )
        }

    fun getRankingForCompetition(competitionConfig: CompetitionConfig): CompetitionRanking = rankingCache[competitionConfig].get()

    private fun updateRankingForCompetition(
        regionId: String,
        competitionId: String,
        poolId: String,
    ): CompetitionRanking {
        return restTemplate.getForEntity(
            "https://api.nevobo.nl/export/poule/$regionId/$competitionId/$poolId/stand.rss",
            String::class.java,
        ).body?.let { responseBody ->
            xmlMapper
                .readValue(responseBody, RssFeed::class.java)
                .internalize()
        } ?: error("Could not fetch loaderboard from Nevobo")
    }

    private fun RssFeed.internalize() =
        CompetitionRanking(
            webUrl = channel.item.first().link,
            lastUpdateTimestamp = channel.item.first().pubDate,
            entries =
                channel.ranking.map { ranking ->
                    RankEntry(
                        number = ranking.nummer.toIntOrDefault(0),
                        team = ranking.team,
                        matches = ranking.wedstrijden.toIntOrDefault(0),
                        points = ranking.punten.toIntOrDefault(0),
                        setsFor = ranking.setsvoor.toIntOrDefault(0),
                        setsAgainst = ranking.setstegen.toIntOrDefault(0),
                        pointsFor = ranking.puntenvoor.toIntOrDefault(0),
                        pointsAgainst = ranking.puntentegen.toIntOrDefault(0),
                    )
                },
        )

    private fun String.toIntOrDefault(default: Int) = toIntOrNull() ?: default
}
