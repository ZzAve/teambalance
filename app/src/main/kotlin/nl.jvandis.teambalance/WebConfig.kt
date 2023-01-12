package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.bank.StringToEnumConverter
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.io.IOException

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToEnumConverter())
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                @Throws(IOException::class)
                override fun getResource(resourcePath: String, location: Resource): Resource {
                    val requestedResource = location.createRelative(resourcePath)
                    return if (requestedResource.exists() && requestedResource.isReadable) {
                        requestedResource
                    } else {
                        ClassPathResource("/static/index.html")
                    }
                }
            })
    }
}
