package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ActivityService {

    private static final String url = "https://fakerestapi.azurewebsites.net/api/v1/Activities";
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final ActivityProducer activityProducer;

    public ActivityService(ActivityProducer activityProducer) {
        this.activityProducer = activityProducer;
    }

    //@Cacheable("activities")
    public List<ActivityResponse> listAllActivities() {
        ActivityResponse[] response = restTemplate.getForObject(url, ActivityResponse[].class);
        return Arrays.asList(response);
    }

    public ActivityResponse createActivity(ActivityResponse activity) {

        // gera id sequencial único
        activity.setId((long) idGenerator.getAndIncrement());

        // gera título com número aleatório
        int randomNumber = ThreadLocalRandom.current().nextInt(1000, 9999);
        activity.setTitle("Activity " + randomNumber);

        // gera dueDate no horário de Brasília
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        OffsetDateTime dueDate = now.atZone(ZoneId.of("America/Sao_Paulo")).toOffsetDateTime();
        activity.setDueDate(dueDate);

        // envia para API externa
        ActivityResponse created = restTemplate.postForObject(url, activity, ActivityResponse.class);

        // publica no Kafka como JSON
        try {
            String json = new ObjectMapper().writeValueAsString(created);
            activityProducer.publishActivity(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return created;
    }
}
