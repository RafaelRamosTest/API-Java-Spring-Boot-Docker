
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.enums.KafkaConfigEnum;
import com.exemple.activity.mapper.ActivityMapper;
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
    private final ActivityMapper activityMapper;

    private final ActivityProducer activityProducer;
    private final ActivityRepository activityRepository;
    private final ActivityLogRepository activityLogRepository; // Novo repositório injetado

    public ActivityService(ActivityMapper activityMapper, ActivityProducer activityProducer,
                           ActivityRepository activityRepository,
                           ActivityLogRepository activityLogRepository) {
        this.activityMapper = activityMapper;
        this.activityProducer = activityProducer;
        this.activityRepository = activityRepository;
        this.activityLogRepository = activityLogRepository;
    }

    // --- FLUXO DO GET ---
    public List<ActivityResponse> listAllActivitiesAndLog(String userId, String route) {
        ActivityResponse[] response = restTemplate.getForObject(url, ActivityResponse[].class);
        List<ActivityResponse> activities = response != null ? Arrays.asList(response) : Collections.emptyList();

        try {
            // 1. Instancia o objeto de log usando o Builder do Lombok
            ActivityLog logData = ActivityLog.builder()
                    .route(route)
                    .timestamp(Instant.now())
                    .totalRecordsConsulted(activities.size())
                    .activities(activities)
                    .build();

            // 2. Envelopa no ActivityEvent com eventType = READ
            ActivityEvent event = ActivityEvent.builder()
                    .eventType(ActivityEvent.EventType.READ)
                    .userId(userId)
                    .payload(logData)
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

        // 1. Garante que a atividade possua um ID (Gera um UUID caso não venha preenchido no request)
        if (activity.getId() == null || activity.getId().isBlank()) {
            activity.setId(java.util.UUID.randomUUID().toString());
        }

        // 2. Publica o evento de CREATE diretamente no Kafka
        try {
            ActivityEvent event = ActivityEvent.builder()
                    .eventType(ActivityEvent.EventType.CREATE) // Identifica como Cadastro
                    .id(activity.getId())
                    .userId(userId)
                    .payload(activity) // O próprio DTO é o payload
                    .build();

            String json = mapper.writeValueAsString(event);
            activityProducer.publishActivity(KafkaConfigEnum.ATIVIDADES, json);

            System.out.println("✅ Usuário " + userId + " publicou o cadastro no Kafka com ID: " + activity.getId());
        } catch (Exception e) {
            System.err.println("❌ Erro ao publicar evento de cadastro no Kafka: " + e.getMessage());
            e.printStackTrace();
        }

        // 3. Retorna o próprio objeto enviado (agora com ID garantido)
        return activity;
    }

    // --- FLUXO DO PUT ---
    public ActivityUpdateRequest updateActivity(String id, ActivityUpdateRequest request, String userId) {

        // 2. Publica o evento de UPDATE no Kafka
        try {
            ActivityEvent event = ActivityEvent.builder()
                    .eventType(ActivityEvent.EventType.UPDATE) // 👈 EventType de Atualização
                    .id(id)
                    .userId(userId)
                    .payload(request)
                    .build();

            String json = mapper.writeValueAsString(event);
            activityProducer.publishActivity(KafkaConfigEnum.ATIVIDADES, json);
            System.out.println("Enviado evento de UPDATE para o Kafka do id: " + id);
        } catch (Exception e) {
            System.err.println("Erro ao publicar update no Kafka: " + e.getMessage());
            e.printStackTrace();
        }

        return request;
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

    /**
     * Atualiza uma atividade existente no MongoDB a partir do evento recebido pelo Kafka.
     *
     * @param event Envelope do evento Kafka contendo metadados (id, userId, eventType)
     * @param dto   Payload com as alterações parciais (title, completed)
     */
    public void updateActivityKafka(ActivityEvent event, ActivityUpdateRequest dto) {
        // 1. Busca o documento existente no MongoDB pelo ID do evento
        Activity existingActivity = activityRepository.findById(Long.valueOf(event.getId()))
                .orElseThrow(() -> new RuntimeException("Atividade não encontrada no MongoDB com ID: " + event.getId()));

        // 2. O MapStruct aplica as alterações parciais do DTO, atualiza o eventType e gera o timestampUpdate
        activityMapper.updateEntityFromDto(event, dto, existingActivity);

        // 3. Salva a entidade atualizada de volta no banco
        activityRepository.save(existingActivity);

        System.out.println("✅ [MongoDB] Atividade atualizada com sucesso. ID: " + event.getId()
                + " | Usuário: " + existingActivity.getUserId()
                + " | Atualizado em: " + existingActivity.getTimestampUpdate());
    }
}