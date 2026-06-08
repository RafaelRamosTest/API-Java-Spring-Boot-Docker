package com.exemple.activity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Configuration
public class Auth0Config {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String auth0Token(RestTemplate restTemplate) {
        String url = "https://meuapp.us.auth0.com/oauth/token";

        String body = """
        {
          "client_id": "SEU_CLIENT_ID",
          "client_secret": "SEU_CLIENT_SECRET",
          "audience": "https://meuapp.us.auth0.com/api/v2/",
          "grant_type": "client_credentials"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        // Aqui você pode usar Jackson para extrair o "access_token" do JSON
        return response.getBody(); // simplificado
    }
}
