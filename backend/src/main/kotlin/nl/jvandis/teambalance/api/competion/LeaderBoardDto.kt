package nl.jvandis.teambalance.api.competion

data class CompetitionRankingDto(
    val webUrl: String,
    val lastUpdateTimestamp: String,
    val entries: List<RankEntryDto>,
)

data class RankEntryDto(
    val number: Int,
    val team: String,
    val matches: Int,
    val points: Int,
    val setsFor: Int,
    val setsAgainst: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
)
