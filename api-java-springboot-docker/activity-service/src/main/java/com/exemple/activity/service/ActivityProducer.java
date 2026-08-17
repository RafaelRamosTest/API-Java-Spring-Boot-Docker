
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.enums.KafkaConfigEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class ActivityProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public ActivityProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    // Recebe o tópico por parâmetro dinamicamente
    public void publishActivity(KafkaConfigEnum config, String activityJson) {
        kafkaTemplate.send(config.getTopic(), activityJson);
    }

    // 🟢 Novo méto-do: recebe o objeto ActivityEvent, converte para JSON e envia usando a chave (ID)
    public void publishEvent(KafkaConfigEnum config, ActivityEvent event) {
        try {
            String jsonMessage = mapper.writeValueAsString(event);

            // Envia passando a chave (event.getId()) para garantir particionamento correto no Kafka
            if (event.getId() != null) {
                kafkaTemplate.send(config.getTopic(), event.getId(), jsonMessage);
            } else {
                kafkaTemplate.send(config.getTopic(), jsonMessage);
            }

            System.out.println("✅ Evento [" + event.getEventType() + "] publicado no tópico [" + config.getTopic() + "] | ID: " + event.getId());
        } catch (Exception e) {
            System.err.println("❌ Erro ao serializar/enviar evento para o Kafka: " + e.getMessage());
            throw new RuntimeException("Falha ao publicar evento no Kafka", e);
        }
    }
}