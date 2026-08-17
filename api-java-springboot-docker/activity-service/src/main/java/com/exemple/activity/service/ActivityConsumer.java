
package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.mapper.ActivityMapper;
import com.exemple.activity.model.Activity;
import com.exemple.activity.model.ActivityLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // 👈 O Lombok gera o construtor automaticamente para todos os campos 'final'
public class ActivityConsumer {

    private final ObjectMapper mapper;
    private final ActivityService activityService;
    private final ActivityMapper activityMapper; // 👈 Injeta o Mapper

    // Listener 1: Focado em escutar e salvar novos CADASTROS (POST)
    @KafkaListener(
            topics = "#{T(com.exemple.activity.enums.KafkaConfigEnum).ATIVIDADES.getTopic()}",
            groupId = "#{T(com.exemple.activity.enums.KafkaConfigEnum).ATIVIDADES.getGroupId()}"
    )
    public void consumeActivities(String message) {
        try {
            System.out.println("📥 [KAFKA CHEGOU]: " + message);

            // 1. Desserializa o envelope principal
            ActivityEvent event = mapper.readValue(message, ActivityEvent.class);

            if (event == null || event.getEventType() == null) {
                System.err.println("⚠️ Mensagem inválida ou sem eventType.");
                return;
            }

            // 2. Roteia de acordo com o tipo de evento do domínio
            switch (event.getEventType()) {

                case CREATE:
                    System.out.println("🟢 Processando evento de CADASTRO (CREATE)...");
                    ActivityCreateRequest createDto = mapper.convertValue(event.getPayload(), ActivityCreateRequest.class);
                    Activity activity = activityMapper.activityEntity(event, createDto);

                    // 🔴 Garantia do TSID como _id no MongoDB
                    String createTsid = (event.getId() != null && !event.getId().isBlank())
                            ? event.getId()
                            : (createDto != null ? createDto.id() : null);
                    activity.setId(createTsid);

                    System.out.println("🔍 [CREATE] Salvando no Mongo com o ID (TSID): " + activity.getId());

                    activityService.saveActivityKafka(activity);
                    System.out.println("✅ [MongoDB] Atividade cadastrada com sucesso!");
                    break;

                case UPDATE:
                    System.out.println("🟡 Processando evento de ATUALIZAÇÃO (UPDATE)...");
                    ActivityUpdateRequest updateDto = mapper.convertValue(event.getPayload(), ActivityUpdateRequest.class);

                    if (updateDto != null) {
                        System.out.println("🔍 [UPDATE] DTO Recebido -> Title: " + updateDto.getTitle() + " | Completed: " + updateDto.getCompleted());
                    }

                    activityService.updateActivityKafka(event, updateDto);
                    System.out.println("✅ [MongoDB] Atividade atualizada com sucesso!");
                    break;

                default:
                    System.out.println("⚠️ Tipo de evento não tratado por este Listener: " + event.getEventType());
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem do Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Listener 2: Focado em escutar e salvar LOGS DE CONSULTA (GET)
    @KafkaListener(
            topics = "#{T(com.exemple.activity.enums.KafkaConfigEnum).LOGS.getTopic()}",
            groupId = "#{T(com.exemple.activity.enums.KafkaConfigEnum).LOGS.getGroupId()}"
    )
    public void consumeLog(String message) {
        try {
            // 1. Desserializa o envelope do evento
            ActivityEvent event = mapper.readValue(message, ActivityEvent.class);

            // 2. Extrai o payload do log
            ActivityLog dto = mapper.convertValue(event.getPayload(), ActivityLog.class);

            // 3. MapStruct funde os metadados do envelope com os dados do log
            ActivityLog log = activityMapper.toActivityLog(event, dto);

            // 4. Salva no MongoDB
            activityService.saveLogKafka(log);

            System.out.println("✅ Log de consulta salvo no MongoDB com sucesso.");
        } catch (Exception e) {
            System.err.println("❌ Erro no processamento do Log no Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }
}