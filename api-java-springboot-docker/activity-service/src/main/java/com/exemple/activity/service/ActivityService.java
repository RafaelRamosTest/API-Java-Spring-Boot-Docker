
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.enums.KafkaConfigEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.exemple.activity.model.Activity;
import com.exemple.activity.model.ActivityLog;
import com.exemple.activity.repository.ActivityRepository;
import com.exemple.activity.repository.ActivityLogRepository;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

@Service
public class ActivityService {

    private static final String url = "https://fakerestapi.azurewebsites.net/api/v1/Activities";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    //private final ObjectMapper mapper = new ObjectMapper();

    private final ActivityProducer activityProducer;
    private final ActivityRepository activityRepository;
    private final ActivityLogRepository activityLogRepository; // Novo repositório injetado

    public ActivityService(ActivityProducer activityProducer,
                           ActivityRepository activityRepository,
                           ActivityLogRepository activityLogRepository) {
        this.activityProducer = activityProducer;
        this.activityRepository = activityRepository;
        this.activityLogRepository = activityLogRepository;
    }

    // --- FLUXO DO GET ---
    public List<ActivityResponse> listAllActivitiesAndLog(String userId) {
        ActivityResponse[] response = restTemplate.getForObject(url, ActivityResponse[].class);
        List<ActivityResponse> activities = response != null ? Arrays.asList(response) : Collections.emptyList();

        try {
            // 1. Cria o objeto interno com os dados da consulta
            ActivityLog logData = new ActivityLog();
            logData.setTimestamp(Instant.now());
            logData.setTotalRecordsConsulted(activities.size());
            logData.setActivities(activities);

            // 2. Envelopa no ActivityEvent com eventType = READ (ou LIST)
            ActivityEvent event = ActivityEvent.builder()
                    .eventType(ActivityEvent.EventType.READ) // 👈 Informa o tipo
                    .userId(userId)
                    .payload(logData) // 👈 O ActivityLog vai dentro do payload
                    .build();

            String jsonLog = mapper.writeValueAsString(event);
            activityProducer.publishActivity(KafkaConfigEnum.LOGS, jsonLog);
        } catch (Exception e) {
            System.err.println("Erro ao publicar log no Kafka: " + e.getMessage());
            e.printStackTrace();
        }

        return activities;
    }

    // --- FLUXO DO POST ---
    public ActivityCreateRequest createActivity(ActivityCreateRequest activity, String userId) {
        // Envia para API externa
        ActivityCreateRequest created = restTemplate.postForObject(url, activity, ActivityCreateRequest.class);

        System.out.println("Usuário " + userId + " registrou a atividade com sucesso na API externa.");

        // Publica no Kafka como um único objeto JSON para o tópico de cadastros
        try {
            ActivityEvent event = ActivityEvent.builder()
                    .eventType(ActivityEvent.EventType.CREATE) // 👈 Identifica o evento como Cadastro
                    .id(created.getId())
                    .userId(userId)
                    .payload(created) // 👈 O objeto da atividade vai aqui dentro
                    .build();
            String json = mapper.writeValueAsString(event);
            activityProducer.publishActivity(KafkaConfigEnum.ATIVIDADES, json); // Tópico exclusivo de Cadastros
        } catch (Exception e) {
            e.printStackTrace();
        }

        return created;
    }

    // --- CONSUMERS (Salvam no Mongo) ---

    // Salva na collection 'activities' (POST)
    public void saveActivityKafka(Activity activity) {
        activityRepository.save(activity);
        System.out.println("✅ [MongoDB] Cadastro salvo na collection 'activities' para o usuário: " + activity.getUserId());
    }

    // Salva na collection 'activity_logs' (GET)
    public void saveLogKafka(ActivityLog log) {
        activityLogRepository.save(log);
        System.out.println("✅ [MongoDB] Log de consulta salvo na collection 'activity_logs'");
    }
}