package nl.jvandis.teambalance

import nl.jvandis.jooq.support.migrations.LiquibaseSupport
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL.using
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.Connection
import java.sql.DriverManager

@Testcontainers
@Transactional("transactionManager")
@SpringBootTest
@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
@AutoConfigureMockMvc
class AbstractIntegrationTest {

//    @Autowired
//    lateinit var webApplicationContext: WebApplicationContext
//    protected lateinit var mockMvc: MockMvc

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            // detect running db

            // if not start testcontainers thing
            postgresqlContainer.start()
            runLiquibaseMigrations()

            TestPropertyValues.of(
                "spring.datasource.username=${postgresqlContainer.username}",
                "spring.datasource.password=${postgresqlContainer.password}",
                "spring.datasource.url=${postgresqlContainer.jdbcUrl}"
            ).applyTo(configurableApplicationContext.environment)
        }

        private fun runLiquibaseMigrations() {
            connection = DriverManager.getConnection(
                postgresqlContainer.jdbcUrl,
                postgresqlContainer.username,
                postgresqlContainer.password
            )

            ctx = using(connection, SQLDialect.POSTGRES)

            connection.createStatement().use { s ->
                log.info("Setting up database")

                log.info("Migrating data with liquibase")
                LiquibaseSupport.migrate(
                    connection,
                    "db.changelog-master.xml",
                    "${System.getProperty("user.dir")}/../backend/src/main/resources/db/changelog/", // TODO: change me to classpath resource
                    "public"
                )
                log.info("Finished setup")
            }
        }
    }

    @BeforeEach
    fun `open a transaction`() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
//            .build();
//        ctx.transaction{x -> DefaultTransactionProvider(x.connectionProvider())}
    }

    @AfterEach
    fun `close a transaction, rolling back any changes`() {
    }

    companion object {

        private var log = loggerFor()
        lateinit var connection: Connection
        lateinit var ctx: DSLContext

        @Container
        val postgresqlContainer = PostgreSQLContainer("postgres:11.18-bullseye").apply {
            withDatabaseName("teambalance")
            withUsername("teambalance")
            withPassword("teambalance")
        }

        @JvmStatic
        @BeforeAll
        fun `setup mockmvc default header`() {
        }
    }
}

internal class IntegrationTestConfig
