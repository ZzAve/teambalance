package nl.jvandis.teambalance

import nl.jvandis.teambalance.api.bank.StringToEnumConverter
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver
import java.io.IOException

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToEnumConverter())
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        // forward requests to from rootpath to the index.html
        registry.addViewController("/").setViewName(
            "forward:/index.html",
        )
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**", "/", "")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(
                object : PathResourceResolver() {
                    @Throws(IOException::class)
                    override fun getResource(
                        resourcePath: String,
                        location: Resource,
                    ): Resource {
                        val tenant =
                            when (MultiTenantContext.getCurrentTenant()) {
                                Tenant.TOVO_HEREN_4 -> "tovoheren4"
                                Tenant.TOVO_HEREN_5 -> "tovoheren5"
                            }
                        val requestedResource = location.createRelative("$tenant/$resourcePath")
                        return if (requestedResource.exists() && requestedResource.isReadable) {
                            requestedResource
                        } else {
                            ClassPathResource("/static/$tenant/index.html")
                        }
                    }
                },
            )
    }
}
