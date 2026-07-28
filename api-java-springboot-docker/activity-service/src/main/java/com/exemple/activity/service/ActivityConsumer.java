
package com.exemple.activity.service;

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
    public void consumeActivityEvent(String message) {
        try {
            // Converte o JSON recebido do Kafka para o envelope do evento
            ActivityEvent event = mapper.readValue(message, ActivityEvent.class);
            ActivityResponse activity = mapper.readValue(message, ActivityResponse.class);

            switch (event.getEventType()) {
                case "ACTIVITY_CREATED":
                    activityService.saveActivityKafka(activity);
                    break;

                case "ACTIVITY_UPDATED":
                    activityService.updateActivityKafka(event.getPayload());
                    break;

                default:
                    System.out.println("⚠️ Tipo de evento não reconhecido: " + event.getEventType());
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar evento de atividade: " + e.getMessage());
        }
    }
    /*public void consumeCreate(String message) {
        try {
            ActivityResponse activity = mapper.readValue(message, ActivityResponse.class);
            activityService.saveActivityKafka(activity);
        } catch (Exception e) {
            System.err.println("Erro no envio do Post: " + e.getMessage());
        }
    }*/

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