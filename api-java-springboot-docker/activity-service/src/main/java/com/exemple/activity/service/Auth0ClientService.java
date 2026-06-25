package com.exemple.activity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Auth0ClientService {

    private final RestTemplate restTemplate;
    private final String domain;
    private final String clientId;
    private final String clientSecret;
    private final String audience;

    public Auth0ClientService(RestTemplate restTemplate,
                              @Value("${auth0.domain}") String domain,
                              @Value("${auth0.clientId}") String clientId,
                              @Value("${auth0.clientSecret}") String clientSecret,
                              @Value("${auth0.audience}") String audience) {
        this.restTemplate = restTemplate;
        this.domain = domain;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.audience = audience;
    }

    public String getAccessToken() {
        String url = "https://" + domain + "/oauth/token";

        String body = """
        {
          "client_id": "%s",
          "client_secret": "%s",
          "audience": "%s",
          "grant_type": "client_credentials"
        }
        """.formatted(clientId, clientSecret, audience);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter access_token do Auth0", e);
        }
    }
}

