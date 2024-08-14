package nl.jvandis.teambalance

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.integration.spring.MultiTenantSpringLiquibase
import liquibase.integration.spring.SpringLiquibase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(LiquibaseProperties::class)
class MultiTenantConfig {
    /**
     * Programmatically disable the 'single tenant' liquibase flow.
     */
    @Bean
    fun liquibase() =
        SpringLiquibase().apply {
            setShouldRun(false)
        }

    @Bean
    @ConditionalOnProperty(prefix = "spring.liquibase", name = ["enabled"])
    @DependsOnDatabaseInitialization
    fun liquibaseMt(
        defaultDataSourceProperties: DataSourceProperties,
        liquibaseProperties: LiquibaseProperties,
    ): MultiTenantSpringLiquibase {
        val adaptedDatasource =
            createLiquibaseDataSourceFrom(defaultDataSourceProperties, liquibaseProperties)

        return MyMultiTenantSpringLiquibase(adaptedDatasource).apply {
            changeLog = liquibaseProperties.changeLog
            defaultSchema = "public"
            schemas = Tenant.entries.map { it.name.lowercase() }
        }
    }

    private fun createLiquibaseDataSourceFrom(
        dataSourceProperties: DataSourceProperties,
        liquibaseProperties: LiquibaseProperties,
    ): HikariDataSource =
        HikariDataSource(
            HikariConfig().apply {
                driverClassName = dataSourceProperties.determineDriverClassName()
                jdbcUrl = dataSourceProperties.determineUrl()
                username = liquibaseProperties.user
                password = liquibaseProperties.password
            },
        )
}

internal class MyMultiTenantSpringLiquibase(private val hikariDataSource: HikariDataSource) : MultiTenantSpringLiquibase() {
    init {
        dataSource = hikariDataSource
    }

    override fun afterPropertiesSet() {
        check(hikariDataSource == dataSource) { "Provided hikariDataSource is not equal to dataSource" }
        log.info("Running MultiTenant liquibase migration")
        super.afterPropertiesSet()
        log.info("Closing HikariDataSource needed for Liquibase migrations")
        hikariDataSource.close()
    }
}
