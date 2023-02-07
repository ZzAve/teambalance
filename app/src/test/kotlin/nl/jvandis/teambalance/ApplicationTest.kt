package nl.jvandis.teambalance

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@Transactional("transactionManager")
@SpringBootTest
internal class ApplicationTest {

    @Test
    fun contextLoads() {
    }
}
