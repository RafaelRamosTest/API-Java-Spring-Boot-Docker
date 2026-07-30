
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.model.ActivityLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class ActivityConsumer {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ActivityService activityService;

    public ActivityConsumer(ActivityService activityService) {
        this.activityService = activityService;
    }

    // Listener 1: Focado em escutar e salvar novos CADASTROS (POST)
    @KafkaListener(
            topics = "#{T(com.exemple.activity.enums.KafkaConfigEnum).ATIVIDADES.getTopic()}",
            groupId = "#{T(com.exemple.activity.enums.KafkaConfigEnum).ATIVIDADES.getGroupId()}"
    )
    public void consumeCreate(String message) {
        try {
            // 2. Extrai o payload para o DTO de criação
            ActivityCreateRequest createRequest = mapper.readValue(message, ActivityCreateRequest.class);

            // 4. Salva no banco passando o ActivityCreateRequest
            activityService.saveActivityKafka(createRequest);

        } catch (Exception e) {
            System.err.println("Erro no processamento da mensagem do Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Listener 2: Focado em escutar e salvar LOGS DE CONSULTA (GET)
    @KafkaListener(
            topics = "#{T(com.exemple.activity.enums.KafkaConfigEnum).LOGS.getTopic()}",
            groupId = "#{T(com.exemple.activity.enums.KafkaConfigEnum).LOGS.getGroupId()}"
    )
    public void consumeLog(String message) {
        try {
            ActivityLog log = mapper.readValue(message, ActivityLog.class);
            activityService.saveLogKafka(log);
        } catch (Exception e) {
            System.err.println("Erro no envio do Log: " + e.getMessage());
        }
    }
}