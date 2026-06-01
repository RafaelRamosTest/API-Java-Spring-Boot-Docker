package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ActivityConsumer {

    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "activities-topic", groupId = "activity-group")
    public void consume(String message) {
        try {
            // Converte o JSON recebido em objeto ActivityResponse
            ActivityResponse activity = mapper.readValue(message, ActivityResponse.class);

            // Imprime os campos de forma estruturada
            System.out.println("Nova atividade recebida:");
            System.out.println("Título: " + activity.getTitle());
            System.out.println("Data: " + activity.getDueDate());

        } catch (Exception e) {
            System.err.println("Erro ao deserializar mensagem: " + message);
            e.printStackTrace();
        }
    }

}

