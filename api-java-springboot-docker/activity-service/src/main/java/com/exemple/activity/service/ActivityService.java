
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
    public List<ActivityResponse> listAllActivitiesAndLog() {
        ActivityResponse[] response = restTemplate.getForObject(url, ActivityResponse[].class);
        List<ActivityResponse> activities = Arrays.asList(response);

        // Dispara para o Kafka informando que uma consulta foi feita (enviando os metadados do Log)
        try {
            ActivityLog log = new ActivityLog();
            log.setQueryTimestamp(LocalDateTime.now());
            log.setTotalRecordsConsulted(activities.size());
            log.setActivities(activities);

            String jsonLog = mapper.writeValueAsString(log);
            activityProducer.publishActivity(KafkaConfigEnum.LOGS, jsonLog); // Tópico exclusivo de Logs
        } catch (Exception e) {
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
            String json = mapper.writeValueAsString(created);
            activityProducer.publishActivity(KafkaConfigEnum.ATIVIDADES, json); // Tópico exclusivo de Cadastros
        } catch (Exception e) {
            e.printStackTrace();
        }

        return created;
    }

    // --- CONSUMERS (Salvam no Mongo) ---

    // Salva na collection 'activities' (POST)
    public void saveActivityKafka(ActivityResponse dto) {
        Activity document = new Activity();
        document.setId(dto.getId());
        document.setTitle(dto.getTitle());
        document.setCompleted(dto.isCompleted());
        activityRepository.save(document);
        System.out.println("✅ [MongoDB] Cadastro salvo na collection 'activities'");
    }

    // Salva na collection 'activity_logs' (GET)
    public void saveLogKafka(ActivityLog log) {
        activityLogRepository.save(log);
        System.out.println("✅ [MongoDB] Log de consulta salvo na collection 'activity_logs'");
    }
}