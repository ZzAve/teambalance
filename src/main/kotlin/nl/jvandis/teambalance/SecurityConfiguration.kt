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
            .antMatchers("/api/bank/**").permitAll()
            .antMatchers("/api/authentication/**").permitAll()
            .antMatchers("/api/users/**").permitAll()
            .antMatchers("/api/trainings/**").permitAll()
            .antMatchers("/api/attendees/**").permitAll()
            .antMatchers("/api/matches/**").permitAll()
            .antMatchers("/api/miscellaneous-events/**").permitAll()
            .antMatchers("/login").permitAll()
            .antMatchers("/_ah/**").permitAll()
            .antMatchers("/internal/**").fullyAuthenticated()
            .antMatchers("/swagger-ui/**").fullyAuthenticated()
            .antMatchers("/webjars/**").fullyAuthenticated()
            .antMatchers("/**.html").permitAll()
            .antMatchers("/**.js").permitAll()
            .antMatchers("/**.css").permitAll()
            .antMatchers("/**.ico").permitAll()
            .antMatchers("/**.png").permitAll()
            .antMatchers("/**.jpg").permitAll()
            .antMatchers("/manifest.json").permitAll()
            .antMatchers("/").permitAll()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .and()
            .httpBasic()
    }
}
