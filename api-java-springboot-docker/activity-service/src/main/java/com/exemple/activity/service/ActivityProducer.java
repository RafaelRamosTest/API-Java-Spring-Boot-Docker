package com.exemple.activity.service;

import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class ActivityProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ActivityProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishActivity(String activityJson) {
        kafkaTemplate.send("activities-topic", activityJson);
    }
}

