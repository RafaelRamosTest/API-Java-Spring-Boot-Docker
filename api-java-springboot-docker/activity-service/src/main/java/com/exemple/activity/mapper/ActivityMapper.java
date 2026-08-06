package com.exemple.activity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.model.Activity;

// 1. Informa ao MapStruct que esta interface é um mapeador de objetos.
// 2. 'componentModel = "spring"' faz o Spring gerenciar essa classe como um Bean (@Component),
//    permitindo que você a injete via construtor no Consumer ou na Service.
@Mapper(componentModel = "spring")
public interface ActivityMapper {
    // Define a regra para o campo 'id' da Entidade (Activity):// Usa código Java direto (expression): se event.getId() não for nulo, usa ele; caso contrário, pega o dto.getId().
    @Mapping(target = "id", expression = "java(event.getId() != null ? event.getId() : dto.getId())")
    // Mapeia o campo 'title' do DTO (dto.title) para o campo 'title' da Entidade (Activity)
    @Mapping(target = "title", source = "dto.title")
    // Mapeia o campo 'completed' do DTO (dto.completed) para o campo 'completed' da Entidade (Activity)
    @Mapping(target = "completed", source = "dto.completed")
    // Extrai o 'userId' do envelope do evento (event.userId) e atribui ao 'userId' da Entidade (Activity)
    @Mapping(target = "userId", source = "event.userId")
    // Converte o Enum 'eventType' do envelope do evento para String (.name()). // A expressão valida se o evento tem um eventType não-nulo antes de converter, evitando NullPointerException.
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : null)")
    // Define o timestamp atual no momento da conversão
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    /* Assinatura do método de conversão:*/ // Recebe dois objetos de entrada (o Envelope do Kafka e o Payload do DTO). // e retorna a Entidade (Activity) totalmente preenchida para ser salva no MongoDB.
    Activity activityEntity(ActivityEvent event, ActivityCreateRequest dto);
}
