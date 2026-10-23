package nl.jvandis.teambalance.api.competion

import org.springframework.boot.jackson.autoconfigure.XmlMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import tools.jackson.databind.MapperFeature

@Configuration
class XmlConfiguration {
    @Bean
    fun xmlMapperCustomizer(): XmlMapperBuilderCustomizer =
        XmlMapperBuilderCustomizer { builder ->
            builder
                .defaultUseWrapper(false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        }
}
