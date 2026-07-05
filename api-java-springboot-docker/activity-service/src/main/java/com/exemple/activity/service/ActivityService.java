
package com.exemple.activity.service;

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
    public ActivityResponse createActivity(ActivityResponse activity, String userId) {
        // Envia para API externa
        ActivityResponse created = restTemplate.postForObject(url, activity, ActivityResponse.class);

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

/*package com.exemple.activity.service;

// Todos os seus imports agora serão utilizados com sucesso!
import com.exemple.activity.dto.ActivityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.exemple.activity.model.Activity;
import com.exemple.activity.repository.ActivityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ActivityService {

    private static final String url = "https://fakerestapi.azurewebsites.net/api/v1/Activities";
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private final ActivityProducer activityProducer;
    private final ActivityRepository activityRepository; // 1. Declarando o repositório do Mongo

    // 2. Injetando AMBOS no construtor (o Producer e o Repository)
    public ActivityService(ActivityProducer activityProducer, ActivityRepository activityRepository) {
        this.activityProducer = activityProducer;
        this.activityRepository = activityRepository;
    }

    // --- FLUXO DO PRODUCER (Envia para o Kafka) ---

    public List<ActivityResponse> listAllActivities() {
        ActivityResponse[] response = restTemplate.getForObject(url, ActivityResponse[].class);
        return Arrays.asList(response);
    }

    public ActivityResponse createActivity(ActivityResponse activity) {
        // Envia para API externa
        ActivityResponse created = restTemplate.postForObject(url, activity, ActivityResponse.class);

        // Publica no Kafka como JSON
        try {
            String json = new ObjectMapper().writeValueAsString(created);
            activityProducer.publishActivity(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return created;
    }

    // --- FLUXO DO CONSUMER (Lê do Kafka e Salva no MongoDB) ---
    // 3. ESTE É O MÉTODO QUE FALTAVA PARA USAR OS IMPORTS!
    public void saveActivityKafka(ActivityResponse dto) {
        System.out.println("📥 [Kafka -> Mongo] Convertendo atividade: " + dto.getTitle());

        // Cria o objeto do MongoDB usando o import 'Activity'
        Activity document = new Activity();
        document.setTitle(dto.getTitle());
        document.setId(dto.getId());
        document.setCompleted(dto.isCompleted());

        // Salva de fato no banco usando o import 'ActivityRepository'
        Activity salva = activityRepository.save(document);
        System.out.println("✅ [MongoDB] Salvo com sucesso! ID gerado pelo Mongo: " + salva.getId());
    }
}*/