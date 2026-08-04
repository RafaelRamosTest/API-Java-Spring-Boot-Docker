
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
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
            // 1. Desserializa o envelope do evento enviado pelo createActivity
            ActivityEvent event = mapper.readValue(message, ActivityEvent.class);

            // 2. Verifica se o evento é de CADASTRO
            if (event.getEventType() == ActivityEvent.EventType.CREATE) {

                // 3. Converte o payload interno para ActivityCreateRequest
                ActivityCreateRequest dto = mapper.convertValue(event.getPayload(), ActivityCreateRequest.class);

                if (event.getId() != null) {
                    dto.setId(event.getId());
                }

                // 4. Manda salvar no MongoDB
                activityService.saveActivityKafka(dto);
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem do Kafka: " + e.getMessage());
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
            // 1. Desserializa o envelope da mensagem
            ActivityEvent event = mapper.readValue(message, ActivityEvent.class);

            // 2. Converte o payload interno para o objeto ActivityLog
            ActivityLog log = mapper.convertValue(event.getPayload(), ActivityLog.class);

            // Optional: se você quiser associar o eventType ou userId dentro do documento de log:
             log.setEventType(event.getEventType().name());
             log.setUserId(event.getUserId());

            // 3. Salva no MongoDB através da Service
            activityService.saveLogKafka(log);

            System.out.println("✅ Log de consulta salvo no MongoDB com sucesso.");
        } catch (Exception e) {
            System.err.println("❌ Erro no processamento do Log no Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
}