package nl.jvandis.teambalance

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import nl.jvandis.teambalance.api.Admin
import nl.jvandis.teambalance.api.Public
import org.springdoc.core.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Method

/**
 *
 * The idea here is that methods can be marked explicitly to be part of a group:
 * Known groups:
 *  - [Admin] or
 *  - [Public]
 *
 *  Per default, an API method is marked as Admin, unless specified otherwise.
 *  There are two levels on which markings can be done:
 *  - On class level, setting the group for all methods within that class
 *  - On method level, setting the group for that specific method.
 *
 *  When combining class and method level, the latter is more explicit, and has therefore a higher precedence.
 *  e.g.
 *  ```
 *      @Admin
 *      class X{
 *          @Public
 *          fun publicEndpoint() = ...
 *
 *          fun adminEndpoint() = ...
 *
 *          (omitted Rest annotations and method impl for explanation purposes)
 *      }
 *  ```
 *
 *
 *  here, `publicEndpoint` will end up in the `Public` group (@[Public] on the method is more specific than @[Admin] on the class)
 *  whereas adminEndpoint will be @[Admin] (as per the class annotation)
 */
@Configuration
class SpringdocConf {

    @Bean
    fun customOpenAPI(@Value("\${app.version}") appVersion: String): OpenAPI? {
        return OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    "basicScheme",
                    SecurityScheme().type(SecurityScheme.Type.APIKEY).`in`(SecurityScheme.In.HEADER).name("X-Secret")
                )
            )
            .security(listOf(SecurityRequirement().addList("basicScheme")))
            .info(
                Info()
                    .title("Teambalance API")
                    .version(appVersion)
            )
    }

    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/*")
            .addOpenApiMethodFilter(this::isPublicMethod)
            .build()
    }

    @Bean
    fun adminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/*")
            .addOpenApiMethodFilter(this::isAdminMethod)
            .build()
    }

    private fun isPublicMethod(method: Method) = !isAdminMethod(method)
    private fun isAdminMethod(method: Method) = !isExplicitlyPublic(method) && isMethodMarkedAsAdmin(method)

    private fun isMethodMarkedAsAdmin(method: Method): Boolean {
        return method.isAnnotationPresent(Admin::class.java) || method.declaringClass.isAnnotationPresent(Admin::class.java)
    }

    private fun isExplicitlyPublic(method: Method) = method.isAnnotationPresent(Public::class.java)
}
