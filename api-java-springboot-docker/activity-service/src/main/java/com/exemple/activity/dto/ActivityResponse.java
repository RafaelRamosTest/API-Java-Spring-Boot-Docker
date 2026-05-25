package com.exemple.activity.dto;

import java.time.OffsetDateTime;

public class ActivityResponse {
    private Long id;
    private String title;
    private OffsetDateTime dueDate;
    private boolean completed;

    // Construtores
    public ActivityResponse() {}

    public ActivityResponse(Long id, String title, OffsetDateTime dueDate, boolean completed) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.completed = completed;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
