package com.exemple.activity.dto;

public class TokenRequest {

    private String username;
    private String password;

    // Construtor vazio (necessário para deserialização JSON)
    public TokenRequest() {
    }

    // Construtor com parâmetros (opcional)
    public TokenRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter e Setter para username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter e Setter para password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

