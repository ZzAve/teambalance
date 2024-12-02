package nl.jvandis.teambalance.api.competion

data class CompetitionRanking(
    val webUrl: String,
    val lastUpdateTimestamp: String,
    val entries: List<RankEntry>,
)

data class RankEntry(
    val number: Int,
    val team: String,
    val matches: Int,
    val points: Int,
    val setsFor: Int,
    val setsAgainst: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
)
