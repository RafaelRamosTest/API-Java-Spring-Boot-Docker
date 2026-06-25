package com.exemple.activity.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.exemple.activity.service.Auth0ClientService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final Auth0ClientService auth0ClientService;

    public AuthController(Auth0ClientService auth0ClientService) {
        this.auth0ClientService = auth0ClientService;
    }

    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> getToken() {
        String accessToken = auth0ClientService.getAccessToken();
        return ResponseEntity.ok(Map.of("access_token", accessToken));
    }
}
