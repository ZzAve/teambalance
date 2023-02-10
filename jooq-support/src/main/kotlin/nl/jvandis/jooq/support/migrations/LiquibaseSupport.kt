package nl.jvandis.jooq.support.migrations

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.DirectoryResourceAccessor
import java.io.File
import java.net.URLDecoder
import java.sql.Connection
import java.sql.SQLException
import kotlin.text.Charsets.UTF_8

/**
 * Helper class to aid in Liquibase driven database migrations. Typical usecase would be for the code generation
 * plugin in combination with Testcontainers.
 *
 * ```
 *  <plugin>
 *      <groupId>org.jooq</groupId>
 *      <artifactId>jooq-codegen-maven</artifactId>
 *      <version>${jooq.version}</version>
 *      <configuration>
 *          <jdbc>
 *              <driver>org.testcontainers.jdbc.ContainerDatabaseDriver</driver>
 *              <url>jdbc:tc:postgresql:${testcontainers.postgresql.version}://testcontainers/unused?TC_INITFUNCTION=nl.jvandis.teambalance.data.LiquibaseSupportKt::migrate&amp;locations=filesystem:${project.basedir}/src/main/resources/db/changelog/db.changelog-master.xml</url>
 *          </jdbc>
 *    </configuration>
 *  </plugin>
 * ```
 */
class LiquibaseSupport {
    companion object {

        @JvmStatic
        @Throws(SQLException::class)
        fun migrate(connection: Connection) {
            val queryParams = getQueryParams(connection.metaData.url)

            val liquibase = Liquibase(
                getParameterValue(queryParams, SCRIPTS),
                DirectoryResourceAccessor(File(getParameterValue(queryParams, ROOT_PATH))), //Verify me
                JdbcConnection(connection)
            )

            // FIXME: Dirty hack to bypass the liquibase.secureParsing bug
            System.setProperty("liquibase.secureParsing", "false")
            System.setProperty("liquibaseSecureParsing", "false")
            System.setProperty("liquibase.secure.parsing", "false")
            System.setProperty("LIQUIBASE_SECURE_PARSING", "false")

            liquibase.database.liquibaseSchemaName = getParameterValue(queryParams, SCHEMA_PARAM)
            if (liquibase.isSafeToRunUpdate) {
                liquibase.update(Contexts(), LabelExpression())
            } else {
                throw IllegalStateException("Cannot run migrations, liquibase does not deem it safe.")
            }
        }

        fun getQueryParams(uri: String): Map<String, String> {
            val queryStringStartIndex: Int = uri.indexOf('?')
            return if (queryStringStartIndex < 0) {
                mapOf()
            } else {
                val query: String = uri.substring(queryStringStartIndex + 1)

                // Maps query1=b&query2=a to Map({key = query1, val = b},{key = query2, val = a})
                return query
                    .split('&')
                    .associate {
                        val split = it.split('=')
                        split[0].decode() to split[1].decode() // query1 to b , query2 to a
                    }
            }
        }

        private fun getParameterValue(queryParameters: Map<String, String>, name: String): String =
            queryParameters[name]
                ?: throw IllegalArgumentException(
                    "Connection query parameter '$name' is missing"
                )

        private fun String.decode(): String = URLDecoder.decode(this, UTF_8)

        private const val SCHEMA_PARAM = "schema"
        private const val ROOT_PATH = "rootPath"
        private const val SCRIPTS = "scripts"
    }
}
