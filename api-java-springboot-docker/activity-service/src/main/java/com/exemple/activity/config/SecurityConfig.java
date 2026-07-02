package com.exemple.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
@EnableMethodSecurity // <-- Adicione isso para ativar as anotações nos controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desabilita o CSRF (essencial para APIs No Stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Configura as regras de autorização
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/token").permitAll() // Libera totalmente esta rota
                        .anyRequest().authenticated()              // Protege todo o resto
                )

                // 3. Configura o Resource Server usando os padrões do application.yml automático
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        // Esse bloco vazio jwt(withDefaults()) diz para o Spring usar o decoder automático do YML

        return http.build();
    }
}

/*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desabilita o CSRF (essencial para APIs que recebem POST sem sessão de navegador)
                .csrf(csrf -> csrf.disable())

                // 2. Configura as regras de autorização
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/token").permitAll() // Libera totalmente esta rota
                        .anyRequest().authenticated()              // Protege todo o resto
                )

                // 3. Configura o Resource Server apenas para as rotas que NÃO são permitidas
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }*/

// Criando o Bean do JwtDecoder manualmente apontando para o seu Auth0
    /*@Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("https://dev-y3883jpsf8nhsfif.us.auth0.com/");
    }*/