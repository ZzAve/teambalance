package nl.jvandis.teambalance.api.training

import nl.jvandis.teambalance.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(MockitoExtension::class)
class TrainingIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
    }

    @Test
    fun basicTraining() {
        val greeting = "Hello Testcontainers with Kotlin"
        mockMvc.post("/api/trainings") {
            content = greeting
            contentType = MediaType.APPLICATION_JSON
            header("X-Secret", "dGVhbWJhbGFuY2U=")
            header("Host", "5.teambalance.local")
        }
            .andExpect {
                status { isBadRequest() }
            }

        mockMvc.get("/api/trainings") {
            header("X-Secret", "dGVhbWJhbGFuY2U=")
            header("Host", "5.teambalance.local")
        }
            .andExpect {
                status { isOk() }
            }
    }
}
