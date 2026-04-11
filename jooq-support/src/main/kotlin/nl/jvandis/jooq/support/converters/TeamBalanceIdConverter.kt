package nl.jvandis.jooq.support.converters

import nl.jvandis.teambalance.TeamBalanceId
import org.jooq.Converter

/**
 * JOOQ converter for Kotlin value class TeamBalanceId.
 *
 * This converter handles the JOOQ bug where value classes are incorrectly serialized.
 * See: https://github.com/jOOQ/jOOQ/issues/14533
 *
 * Without this converter, JOOQ would serialize TeamBalanceId as {"value": true} instead
 * of the actual string value.
 */
class TeamBalanceIdConverter : Converter<String, TeamBalanceId> {
    override fun from(databaseObject: String?): TeamBalanceId? = if (databaseObject == null) null else TeamBalanceId(databaseObject)

    override fun to(userObject: TeamBalanceId?): String? = userObject?.value

    override fun fromType(): Class<String> = String::class.java

    override fun toType(): Class<TeamBalanceId> = TeamBalanceId::class.java
}
