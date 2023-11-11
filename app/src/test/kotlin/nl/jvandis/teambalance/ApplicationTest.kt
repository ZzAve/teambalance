package nl.jvandis.teambalance

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@Transactional("transactionManager")
@SpringBootTest
@Disabled
// Disables because it clashes with integration tests derived from AbstractIntegrationTest.
internal class ApplicationTest {
    @Test
    fun contextLoads() {
    }
}
