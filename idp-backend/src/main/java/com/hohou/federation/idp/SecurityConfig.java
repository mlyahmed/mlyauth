package com.hohou.federation.idp;

import com.hohou.federation.idp.authentication.rs.JOSEBearerAuthenticationFilter;
import com.hohou.federation.idp.authentication.sp.jose.JOSEAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.RequestContextFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final int ENCODING_STRENGTH = 4;


    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SAMLUserDetailsService samlUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(ENCODING_STRENGTH);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    @Bean
    public JOSEAuthenticationProvider joseAuthenticationProvider() {
        return new JOSEAuthenticationProvider();
    }


    @Autowired
    public void configAuthenticationProvider(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthProvider());
        auth.authenticationProvider(samlAuthenticationProvider());
        auth.authenticationProvider(joseAuthenticationProvider());
    }

    @Bean("requestContextFilter")
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }


    @Bean("authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public JOSEBearerAuthenticationFilter joseBearerAuthenticationFilter() {
        return new JOSEBearerAuthenticationFilter();
    }

    //CHECKSTYLE:OFF
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        http.httpBasic();
        http.authorizeRequests()
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/sp/jose/**").permitAll()
                .antMatchers("/sp/saml/**").permitAll()
                .antMatchers("/idp/saml/**").permitAll()
                .antMatchers("/401.html").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/401.html")
                .failureUrl("/401.html")
                .successForwardUrl("/home")
                .and()
                .logout()
                .logoutSuccessUrl("/401.html");

        http.addFilterBefore(joseBearerAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(requestContextFilter(), ChannelProcessingFilter.class);
        http.csrf().disable();
    }
    //CHECKSTYLE:ON
}
