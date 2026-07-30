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
    private String id;          // Útil para rastreio direto e fundamental no DELETE
    private String userId;      // Quem realizou a ação
    private Object payload;     // Dados da atividade (DTO de Request/Response)
}

/*
@Data: Substituiu todos os getters, setters, toString(), equals() e hashCode().

@NoArgsConstructor: Cria o construtor padrão sem argumentos (public ActivityCreateRequest() {}), necessário para frameworks como Jackson (serialização JSON) e Spring.

@AllArgsConstructor: Cria o construtor com todos os parâmetros que você tinha definido manualmente.
*/