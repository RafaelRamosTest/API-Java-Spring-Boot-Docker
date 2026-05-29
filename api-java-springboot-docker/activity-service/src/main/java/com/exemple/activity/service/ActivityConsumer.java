package com.exemple.activity.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ActivityConsumer {

    @KafkaListener(topics = "activities-topic", groupId = "activity-group")
    public void consume(String message) {
        System.out.println("Nova atividade recebida: " + message);
    }
}

