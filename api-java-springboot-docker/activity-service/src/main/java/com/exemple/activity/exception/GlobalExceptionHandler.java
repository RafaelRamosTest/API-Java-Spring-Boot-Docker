package com.exemple.activity.exception;

import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.enums.KafkaConfigEnum;
import com.exemple.activity.model.ActivityLog;
import com.exemple.activity.service.ActivityProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.ServiceUnavailableException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ActivityProducer activityProducer;
    private final ObjectMapper mapper;

    // Erros de Validação dos DTOs (@Valid / @NotBlank / @Size) - HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String route = request.getRequestURI();
        String errorMessage = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Erro de validação nos campos informados";

        publishErrorLog(userId, route, HttpStatus.BAD_REQUEST.value(), errorMessage);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Erro de Validação");
        body.put("message", errorMessage);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Serviço Indisponível - HTTP 503
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Object> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String route = request.getRequestURI();

        publishErrorLog(userId, route, HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "Serviço indisponível");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Exceções genéricas - HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalServerErrorException(Exception ex, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        String route = request.getRequestURI();

        publishErrorLog(userId, route, HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Erro interno no servidor");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void publishErrorLog(String userId, String route, int statusCode, String errorMessage) {
        try {
            ActivityLog errorLog = ActivityLog.builder()
                    .eventType("CREATE")
                    .userId(userId)
                    .route(route)
                    .timestamp(Instant.now())
                    .statusCode(statusCode)
                    .status("ERROR")
                    .errorMessage(errorMessage)
                    .build();

            ActivityEvent event = ActivityEvent.builder()
                    .eventType(ActivityEvent.EventType.CREATE)
                    .userId(userId)
                    .payload(errorLog)
                    .build();

            activityProducer.publishActivity(KafkaConfigEnum.LOGS, mapper.writeValueAsString(event));
        } catch (Exception e) {
            System.err.println("❌ Erro ao publicar log de exceção no Kafka: " + e.getMessage());
        }
    }
}
