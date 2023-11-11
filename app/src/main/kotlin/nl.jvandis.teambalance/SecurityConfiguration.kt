package nl.jvandis.teambalance

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration {
    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .headers().frameOptions().sameOrigin().and()
            .csrf().disable()
            .authorizeHttpRequests()
            // Permit public API calls
            .requestMatchers("/api/**").permitAll()
            // Require auth on internal pages
            .requestMatchers("/internal/**").fullyAuthenticated()
            .requestMatchers("/webjars/**").fullyAuthenticated()
            // Be lean on resource files
            .requestMatchers("/**.html").permitAll()
            .requestMatchers("/**.js").permitAll()
            .requestMatchers("/**.css").permitAll()
            .requestMatchers("/**.ico").permitAll()
            .requestMatchers("/**.png").permitAll()
            .requestMatchers("/**.jpg").permitAll()
            .requestMatchers("/manifest.json").permitAll()
            // Permit frontend paths
            .requestMatchers("/_ah/**").permitAll() // check this one!
            .requestMatchers("/**").permitAll()
            .anyRequest().authenticated() // default is authenticated
            .and()
            .formLogin()
            .and()
            .httpBasic()

        return http.build()
    }
}
