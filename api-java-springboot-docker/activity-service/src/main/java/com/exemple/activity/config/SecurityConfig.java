package com.exemple.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/token").permitAll() // libera o endpoint de token
                        .anyRequest().authenticated()              // protege todos os outros
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt()); // valida JWT automaticamente

        return http.build();
    }
}
