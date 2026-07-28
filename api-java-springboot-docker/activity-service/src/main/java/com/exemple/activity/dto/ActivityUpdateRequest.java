package com.exemple.activity.dto;

public class ActivityUpdateRequest {

    private String id;
    private String title;
    private Boolean completed; // Usa Boolean (B maiúsculo) para permitir null

    // Construtor vazio (necessário para o Jackson/Spring desserializar o JSON)
    public ActivityUpdateRequest() {
    }

    // Construtor completo
    public ActivityUpdateRequest(String id, String title, Boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    // --- GETTERS E SETTERS ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
