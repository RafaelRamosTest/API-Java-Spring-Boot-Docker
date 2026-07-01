package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityConsumer {

    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "activities-topic", groupId = "activity-group")
    public void consume(String message) {
        try {
            // Altere usando o TypeReference para manter o tipo genérico correto da lista
            List<ActivityResponse> activities = mapper.readValue(message, new TypeReference<List<ActivityResponse>>() {});

            System.out.println("Pacote de atividades recebido. Total: " + activities.size());

            for (ActivityResponse activity : activities) {
                System.out.println("Nova atividade recebida:");
                System.out.println("Título: " + activity.getTitle());
            }

        } catch (Exception e) {
            System.err.println("Erro ao deserializar mensagem: " + message);
            e.printStackTrace();
        }
    }

}

