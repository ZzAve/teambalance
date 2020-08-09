package nl.jvandis.teambalance

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
// @EnableSwagger2
class SpringFoxConfig {

    @Bean
    fun apis(): Docket {
        val path = "/api/.*"
        return Docket(DocumentationType.SWAGGER_2)
//                .groupName("api")
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(regex(path))
            .build()
    }
}
//
// private fun metaData(): ApiInfo {
//    val apiInfo =  ApiInfo(
//            "Team Balance REST API",
//            "Spring Boot REST API for Tovo Heren 5",
//            "1.0",
//            "",
//             Contact ("Julius van Dis", "", "vandis.j@gmail.com"),
//    "Apache License Version 2.0",
//    "https://www.apache.org/licenses/LICENSE-2.0",
//            null);
//    return apiInfo;
// }
