package nl.jvandis.teambalance
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MicronautTest
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class TeambalanceTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Test
    @Ignore
    fun testItWorks() {
        Assertions.assertTrue(application.isRunning)
    }

}