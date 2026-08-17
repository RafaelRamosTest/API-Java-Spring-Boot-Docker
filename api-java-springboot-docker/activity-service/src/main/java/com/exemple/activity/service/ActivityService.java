
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.enums.KafkaConfigEnum;
import com.exemple.activity.mapper.ActivityMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.tsid.TSID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
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
        try {
            // 1. Executa a chamada retornando o ResponseEntity com o StatusCode HTTP
            ResponseEntity<ActivityResponse[]> responseEntity = restTemplate.getForEntity(url, ActivityResponse[].class);

            int httpStatus = responseEntity.getStatusCode().value(); // Ex: 200, 204
            ActivityResponse[] body = responseEntity.getBody();
            List<ActivityResponse> activities = body != null ? Arrays.asList(body) : Collections.emptyList();

            // 2. Monta o Log de SUCESSO
            ActivityLog successLog = ActivityLog.builder()
                    .route(route)
                    .timestamp(Instant.now())
                    .totalRecordsConsulted(activities.size())
                    .activities(activities)
                    .statusCode(httpStatus)
                    .status("SUCCESS")
                    .build();

            // 3. Publica o evento de SUCESSO no Kafka
            publishKafkaLog(userId, successLog);

            return activities;

        } catch (HttpStatusCodeException e) {
            // 3. Captura erros HTTP da API externa (ex: 400, 404, 500)
            int httpStatus = e.getStatusCode().value();
            System.err.println("❌ Erro HTTP " + httpStatus + " da API externa: " + e.getMessage());

            ActivityLog errorLog = ActivityLog.builder()
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(httpStatus)
                    .status("ERROR")
                    .totalRecordsConsulted(0)
                    .activities(Collections.emptyList())
                    .errorMessage(e.getResponseBodyAsString().isEmpty() ? e.getMessage() : e.getResponseBodyAsString())
                    .build();

            publishKafkaLog(userId, errorLog);
            throw e;
        } catch (Exception e) {
            // 4. Captura falhas de infraestrutura/conexão (Timeout, DNS, etc.)
            System.err.println("❌ Erro de conexão/infraestrutura: " + e.getMessage());

            ActivityLog errorLog = ActivityLog.builder()
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(500) // Fallback para erro de servidor/conexão
                    .status("ERROR")
                    .totalRecordsConsulted(0)
                    .activities(Collections.emptyList())
                    .errorMessage("Erro de integração/conexão: " + e.getMessage())
                    .build();

            publishKafkaLog(userId, errorLog);
            throw e;
        }
    }

    // Méto-do auxiliar isolado para publicar no Kafka sem duplicar código
    private void publishKafkaLog(String userId, ActivityLog logData) {
        try {
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
    }

    // --- FLUXO DO POST ---
    public ActivityCreateRequest createActivity(ActivityCreateRequest activity, String userId, String route) {

        if (activity.id() == null || activity.id().isBlank()) {
            activity = new ActivityCreateRequest(
                    String.valueOf(io.hypersistence.tsid.TSID.fast().toLong()),
                    activity.title(),
                    activity.completed()
            );
        }

        try {
            // 2. Evento do Domínio (Tópico ATIVIDADES -> Salva na coleção 'activities')
            ActivityEvent domainEvent = ActivityEvent.builder()
                    .id(activity.id())
                    .eventType(ActivityEvent.EventType.CREATE)
                    .userId(userId)
                    .payload(activity) // 👈 Envia o DTO direto (contém title e completed na raiz)
                    .build();

            activityProducer.publishEvent(KafkaConfigEnum.ATIVIDADES, domainEvent);

            // 3. Evento de Auditoria (Tópico LOGS -> Salva na coleção 'activity_logs')
            ActivityLog successLog = ActivityLog.builder()
                    .activityId(activity.id())
                    .eventType("CREATE")
                    .userId(userId)
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(200)
                    .status("SUCCESS")
                    .payload(activity)
                    .build();

            ActivityEvent logEvent = ActivityEvent.builder()
                    .id(activity.id())
                    .eventType(ActivityEvent.EventType.CREATE)
                    .userId(userId)
                    .payload(successLog)
                    .build();

            activityProducer.publishEvent(KafkaConfigEnum.LOGS, logEvent);

            System.out.println("✅ Cadastro publicado nos tópicos do Kafka com ID: " + activity.id());
            return activity;

        } catch (Exception e) {
            System.err.println("❌ Erro ao publicar evento de cadastro: " + e.getMessage());

            // 4. Em caso de falha, publica apenas no tópico de LOGS
            ActivityLog errorLog = ActivityLog.builder()
                    .activityId(activity.id())
                    .eventType("CREATE")
                    .userId(userId)
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(500)
                    .status("ERROR")
                    .errorMessage("Falha no cadastro: " + e.getMessage())
                    .payload(activity)
                    .build();

            ActivityEvent errorEvent = ActivityEvent.builder()
                    .id(activity.id())
                    .eventType(ActivityEvent.EventType.CREATE)
                    .userId(userId)
                    .payload(errorLog)
                    .build();

            activityProducer.publishEvent(KafkaConfigEnum.LOGS, errorEvent);

            throw new RuntimeException("Erro ao processar cadastro da atividade: " + e.getMessage(), e);
        }
    }

    // --- FLUXO DO PUT ---
    public ActivityUpdateRequest updateActivity(String id, ActivityUpdateRequest request, String userId, String route) {

        try {
            // 1. Evento de DOMÍNIO -> Tópico ATIVIDADES (Atualiza a coleção 'activities')
            ActivityEvent domainEvent = ActivityEvent.builder()
                    .id(id)
                    .eventType(ActivityEvent.EventType.UPDATE)
                    .userId(userId)
                    .payload(request) // 👈 DTO limpo (title e completed na raiz)
                    .build();

            activityProducer.publishEvent(KafkaConfigEnum.ATIVIDADES, domainEvent);

            // 2. Evento de AUDITORIA -> Tópico LOGS (Salva na coleção 'activity_logs')
            // Gera um ID único e exclusivo para o DOCUMENTO DE LOG
            String logId = String.valueOf(TSID.fast().toLong());
            ActivityLog successLog = ActivityLog.builder()
                    .id(logId)              // 👈 ID único do Log
                    .activityId(id)         // 👈 ID do recurso editado
                    .eventType("UPDATE")
                    .userId(userId)
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(200)
                    .status("SUCCESS")
                    .payload(request)
                    .build();

            ActivityEvent logEvent = ActivityEvent.builder()
                    .id(logId)              // 👈 Chave única da mensagem no Kafka
                    .eventType(ActivityEvent.EventType.UPDATE)
                    .userId(userId)
                    .payload(successLog)
                    .build();

            activityProducer.publishEvent(KafkaConfigEnum.LOGS, logEvent);

            System.out.println("✅ UPDATE enviado com sucesso para os tópicos ATIVIDADES e LOGS | ID: " + id);
            return request;

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar atualização: " + e.getMessage());

            // Log de ERRO enviado apenas para o tópico LOGS
            ActivityLog errorLog = ActivityLog.builder()
                    .activityId(id)
                    .eventType("UPDATE")
                    .userId(userId)
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(500)
                    .status("ERROR")
                    .errorMessage("Falha no update: " + e.getMessage())
                    .payload(request)
                    .build();

            ActivityEvent errorEvent = ActivityEvent.builder()
                    .id(id)
                    .eventType(ActivityEvent.EventType.UPDATE)
                    .userId(userId)
                    .payload(errorLog)
                    .build();

            activityProducer.publishEvent(KafkaConfigEnum.LOGS, errorEvent);

            throw new RuntimeException("Erro ao processar atualização da atividade: " + e.getMessage(), e);
        }
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