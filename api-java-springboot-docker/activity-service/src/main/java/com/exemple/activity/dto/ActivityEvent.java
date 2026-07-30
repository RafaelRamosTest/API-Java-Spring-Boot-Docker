package com.exemple.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEvent {

    public enum EventType {
        CREATE,
        UPDATE,
        DELETE
    }

    private EventType eventType; // CREATE, UPDATE ou DELETE
    private Long id;          // Útil para rastreio direto e fundamental no DELETE
    private String userId;      // Quem realizou a ação
    private Object payload;     // Dados da atividade (DTO de Request/Response)
}