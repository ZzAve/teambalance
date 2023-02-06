package nl.jvandis.jooq.support.migrations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiquibaseSupportTest {

    @Test
    fun `just testing`() {
    }

    @Test
    fun `query params are split okay`() {
        val queryParams = LiquibaseSupport.getQueryParams("http://url.domain?hello=1&hello=2&HELLO=3")

        assertEquals(queryParams.size, 2)
        assertTrue { queryParams.entries.any { (k, v) -> k == "hello" && v == "2" } }
        assertTrue { queryParams.entries.any { (k, v) -> k == "HELLO" && v == "3" } }
    }
}
