package org.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.webapp.handler.JwtAuthenticationFilter;
import org.webapp.service.UserServiceImpl;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
    @Autowired
    private UserServiceImpl userDetailsService;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(settings -> settings
                .requestMatchers("/user/register", "/user/login", "/user/info",
                        "/video/list", "/video/watch", "/video/popular", "/video/search",
                        "/like/list", "/comment/list")
                .permitAll()
                .requestMatchers("/user/avatar/upload", "/video/publish",
                        "/like/action", "/comment/publish",
                        "/relation/action", "follower/list", "/following/list", "/friends/list", "/block/list",
                        "/chat").hasRole("USER")
                .requestMatchers("/user/auth", "/comment/delete").hasRole("ADMIN")
                .anyRequest().denyAll());
        httpSecurity.addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class);
        httpSecurity.exceptionHandling(settings -> settings.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler));
        httpSecurity.userDetailsService(userDetailsService);
        httpSecurity.sessionManagement(settings -> settings.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.csrf(settings -> settings.disable());
        return httpSecurity.build();
    }
}
