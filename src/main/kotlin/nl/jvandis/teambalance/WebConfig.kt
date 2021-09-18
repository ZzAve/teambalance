package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.bank.StringToEnumConverter
import org.springframework.context.annotation.Configuration

import org.springframework.format.FormatterRegistry

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToEnumConverter())
    }
}
