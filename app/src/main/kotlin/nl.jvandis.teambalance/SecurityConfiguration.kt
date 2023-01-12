package nl.jvandis.teambalance

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

//    @Throws(Exception::class)
//    override fun configure(web: WebSecurity) {
//        web
//                .ignoring()
//                .antMatchers("/resources/**")
//    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .headers().frameOptions().sameOrigin().and()
            .csrf().disable()
            .authorizeRequests()
            // Permit public API calls
            .antMatchers("/api/**").permitAll()
            // Require auth on internal pages
            .antMatchers("/internal/**").fullyAuthenticated()
            .antMatchers("/webjars/**").fullyAuthenticated()
            // Be lean on resource files
            .antMatchers("/**.html").permitAll()
            .antMatchers("/**.js").permitAll()
            .antMatchers("/**.css").permitAll()
            .antMatchers("/**.ico").permitAll()
            .antMatchers("/**.png").permitAll()
            .antMatchers("/**.jpg").permitAll()
            .antMatchers("/manifest.json").permitAll()
            // Permit frontend paths
            .antMatchers("/_ah/**").permitAll() // check this one!
            .antMatchers("/**").permitAll()
            .anyRequest().authenticated() // default is authenticated
            .and()
            .formLogin()
            .and()
            .httpBasic()
    }
}
