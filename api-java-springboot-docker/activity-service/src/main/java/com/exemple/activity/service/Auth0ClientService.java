package com.exemple.activity.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Auth0ClientService {

    private final RestTemplate restTemplate;
    private final String domain = "https://dev-y3883jpsf8nhsfif.us.auth0.com/api/v2/users";
    private final String jwtToken;

    public Auth0ClientService(RestTemplate restTemplate, String auth0Token) {
        this.restTemplate = restTemplate;
        this.jwtToken = auth0Token; // injetado pelo Auth0Config
    }

    public String createUser(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);

        String body = """
        {
          "email": "%s",
          "password": "%s",
          "connection": "Username-Password-Authentication"
        }
        """.formatted(email, password);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(domain, request, String.class);

        return response.getBody();
    }
}

