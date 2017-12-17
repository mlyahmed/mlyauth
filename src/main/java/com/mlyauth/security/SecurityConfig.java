package com.mlyauth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private BasicAuthenticationProvider basicAUth;

    @Autowired
    public void configAuthenticationProvider(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(basicAUth);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/index.html").permitAll()
                .anyRequest().authenticated();

        http.csrf().disable();

        http.formLogin()
                .loginPage("/person/login")
                .loginProcessingUrl("/authenticate")
                .failureForwardUrl("/person/login?error=true")
                .successForwardUrl("/index.html")
                .permitAll();

        http.httpBasic();
        http.logout().logoutSuccessUrl("/person/login?logout=true").permitAll();
    }
}
