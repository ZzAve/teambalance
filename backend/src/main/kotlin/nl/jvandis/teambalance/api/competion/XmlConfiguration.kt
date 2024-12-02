package nl.jvandis.teambalance.api.competion

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class XmlConfiguration {
    @Bean
    @Qualifier("xmlMapper")
    fun xmlMapper(): ObjectMapper =
        XmlMapper(
            JacksonXmlModule().apply {
                setDefaultUseWrapper(false)
            },
        )
            .registerKotlinModule()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
}
