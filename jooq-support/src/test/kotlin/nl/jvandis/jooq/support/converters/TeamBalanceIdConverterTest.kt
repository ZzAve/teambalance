package nl.jvandis.jooq.support.converters

import nl.jvandis.teambalance.TeamBalanceId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamBalanceIdConverterTest {
    private val converter = TeamBalanceIdConverter()

    @Test
    fun `from converts database string to TeamBalanceId`() {
        val databaseValue = "test-id-123"
        val result = converter.from(databaseValue)
        assertThat(result).isNotNull
        assertThat(result?.value).isEqualTo(databaseValue)
    }

    @Test
    fun `from returns null when database value is null`() {
        val result = converter.from(null)
        assertThat(result).isNull()
    }

    @Test
    fun `to converts TeamBalanceId back to string`() {
        val id = TeamBalanceId("test-id-456")
        val result = converter.to(id)
        assertThat(result).isEqualTo("test-id-456")
    }

    @Test
    fun `to returns null when user object is null`() {
        val result = converter.to(null)
        assertThat(result).isNull()
    }

    @Test
    fun `round-trip conversion preserves value`() {
        val originalValue = "round-trip-test-789"
        val teamBalanceId = converter.from(originalValue)
        val roundTripValue = converter.to(teamBalanceId)
        assertThat(roundTripValue).isEqualTo(originalValue)
    }

    @Test
    fun `fromType returns String class`() {
        assertThat(converter.fromType()).isEqualTo(String::class.java)
    }

    @Test
    fun `toType returns TeamBalanceId class`() {
        assertThat(converter.toType()).isEqualTo(TeamBalanceId::class.java)
    }
}
