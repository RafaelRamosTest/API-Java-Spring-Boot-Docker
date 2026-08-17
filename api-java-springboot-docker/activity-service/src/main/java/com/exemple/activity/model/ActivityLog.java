package com.exemple.activity.model;

import com.exemple.activity.dto.ActivityResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "activity_logs")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityLog {

    @Id
    private String id;
    private String activityId;       // ID da atividade gerada (TSID)
    private String eventType;        // "CREATE"
    private String userId;           // Quem executou a requisição
    private Instant timestamp;       // Data/Hora da operação
    private String route;            // Endpoint/Rota acessada

    // 🔴 Status e Erro
    private Integer statusCode;      // Código HTTP (ex: 201, 400, 500)
    private String status;           // "SUCCESS" ou "ERROR"
    private String errorMessage;     // Detalhes da exceção em caso de falha

    // ✏️ Campos específicos por tipo de operação
    private Object payload;                // Dados do cadastro enviados (POST/PUT)
    private Integer totalRecordsConsulted; // Quantidade de registros (GET)
    private List<ActivityResponse> activities; // Lista de resultados (GET)
}
