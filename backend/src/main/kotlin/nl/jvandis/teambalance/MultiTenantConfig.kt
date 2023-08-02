package nl.jvandis.teambalance

import liquibase.integration.spring.MultiTenantSpringLiquibase
import liquibase.integration.spring.SpringLiquibase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(LiquibaseProperties::class)
class MultiTenantConfig {

    /**
     * Programmatically disable the 'single tenant' liquibase flow.
     */
    @Bean
    fun liquibase() = SpringLiquibase().apply {
        setShouldRun(false)
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.liquibase", name = ["enabled"])
    fun liquibaseMt(
        sqlDataSource: DataSource,
        liquibaseProperties: LiquibaseProperties
    ) = MultiTenantSpringLiquibase().apply {
        changeLog = liquibaseProperties.changeLog
        defaultSchema = "public"
        dataSource = sqlDataSource
        schemas = Tenant.values().map { it.name.lowercase() }
    }
}
