package nl.jvandis.teambalance.api.settings

import nl.jvandis.teambalance.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

private const val HOST = "5.teambalance.local"
private const val SECRET = "dGVhbWJhbGFuY2U=" // base64("teambalance")
private const val SEASON_PATH = "/api/config/season"

class SeasonConfigControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET season config is accessible without admin role`() {
        mockMvc
            .get(SEASON_PATH) {
                header("X-Secret", SECRET)
                header("Host", HOST)
            }.andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.startOfSeason") { exists() }
            }
    }

    @Test
    @WithMockUser(roles = ["admin"])
    fun `PUT season config updates start of season and GET returns updated value`() {
        mockMvc
            .put(SEASON_PATH) {
                contentType = MediaType.APPLICATION_JSON
                content = """{"startOfSeason": "2025-08-02T00:00:00"}"""
                header("X-Secret", SECRET)
                header("Host", HOST)
            }.andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.startOfSeason") { value("2025-08-02T00:00:00") }
            }

        mockMvc
            .get(SEASON_PATH) {
                header("X-Secret", SECRET)
                header("Host", HOST)
            }.andExpect {
                status { isOk() }
                jsonPath("$.startOfSeason") { value("2025-08-02T00:00:00") }
            }
    }

    @Test
    fun `PUT season config without admin role returns 403`() {
        mockMvc
            .put(SEASON_PATH) {
                contentType = MediaType.APPLICATION_JSON
                content = """{"startOfSeason": "2025-08-02T00:00:00"}"""
                header("X-Secret", SECRET)
                header("Host", HOST)
            }.andExpect {
                status { isForbidden() }
            }
    }
}
