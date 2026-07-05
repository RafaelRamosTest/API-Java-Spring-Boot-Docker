
package com.exemple.activity.service;

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
            ActivityResponse activity = mapper.readValue(message, ActivityResponse.class);
            activityService.saveActivityKafka(activity);
        } catch (Exception e) {
            System.err.println("Erro no envio do Post: " + e.getMessage());
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

/*package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityConsumer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ActivityService activityService; // 1. Declara a dependência do Service

    // 2. Injeta o ActivityService pelo construtor
    public ActivityConsumer(ActivityService activityService) {
        this.activityService = activityService;
    }

    @KafkaListener(topics = "activities-topic", groupId = "activity-group")
    public void consume(String message) {
        try {
            // Deserializa o JSON que veio do Kafka em uma lista de DTOs
            List<ActivityResponse> activities = mapper.readValue(message, new TypeReference<List<ActivityResponse>>() {});

            System.out.println("Pacote de atividades recebido. Total: " + activities.size());

            // 3. Varre a lista e chama a lógica de negócio para salvar no MongoDB
            for (ActivityResponse activity : activities) {
                System.out.println("Nova atividade recebida: " + activity.getTitle());

                // MÁGICA AQUI: Envia para o service converter e salvar no Mongo!
                activityService.saveActivityKafka(activity);
            }

        } catch (Exception e) {
            System.err.println("Erro ao deserializar mensagem: " + message);
            e.printStackTrace();
        }
    }
}*/

