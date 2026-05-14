package com.saas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/tenants/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .httpBasic(httpBasic -> {});
        return http.build();
    }
}
