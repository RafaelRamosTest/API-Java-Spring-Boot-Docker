package com.exemple.activity.mapper;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.model.Activity;
import com.exemple.activity.model.ActivityLog;
import org.mapstruct.*;

import java.time.Instant;

// Indica ao MapStruct que é uma interface de mapeamento e faz o Spring gerenciá-la como Bean (@Component), além de importar a classe Instant e ActivityEvent para uso nas expressões Java.
@Mapper(componentModel = "spring", imports = {Instant.class, ActivityEvent.class})
public interface ActivityMapper {

    // Preenche o ID da entidade preferencialmente com o ID do evento (se existir); caso contrário, usa o ID gerado pelo TSID dentro do Record dto.
    @Mapping(target = "id", expression = "java(event.getId() != null ? event.getId() : dto.id())")
    // Mapeia o campo 'title' do Record dto para o campo 'title' da entidade Activity.
    @Mapping(target = "title", source = "dto.title")
    // Mapeia o campo 'completed' do Record dto para o campo 'completed' da entidade Activity.
    @Mapping(target = "completed", source = "dto.completed")
    // Mapeia o 'userId' contido no envelope do evento Kafka para o campo 'userId' da entidade Activity.
    @Mapping(target = "userId", source = "event.userId")
    // Converte o Enum do tipo de evento (eventType) para String; se o tipo for nulo, atribui valor nulo.
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : null)")
    // Gera o timestamp da data/hora atual no momento da conversão para marcar a criação do registro.
    @Mapping(target = "timestamp", expression = "java(Instant.now())")
    // Gera o timestamp da data/hora atual no momento da conversão para a última atualização do registro.
    @Mapping(target = "timestampUpdate", expression = "java(Instant.now())")
    // Converte os dados do evento do Kafka (ActivityEvent) e do payload (ActivityCreateRequest) em uma entidade Activity preenchida.
    Activity activityEntity(ActivityEvent event, ActivityCreateRequest dto);

    // Ignora a atribuição do ID para que o MongoDB possa gerenciar o ID do documento de log automaticamente.
    @Mapping(target = "id", ignore = true)
    // Extrai o 'userId' do evento Kafka e o insere no registro de log.
    @Mapping(target = "userId", source = "event.userId")
    // Converte o Enum do tipo de evento em String de forma segura para gravar no log.
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : null)")
    // Converte os dados do evento do Kafka e o objeto ActivityLog de origem em um novo documento ActivityLog.
    ActivityLog toActivityLog(ActivityEvent event, ActivityLog dto);

    // Configura o MapStruct para ignorar propriedades nulas recebidas no DTO, permitindo atualizações parciais.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // Ignora a alteração do ID para manter o identificador original já existente no MongoDB.
    @Mapping(target = "id", ignore = true)
    // Ignora a alteração da data de criação original para preservar o histórico de quando a entidade foi criada.
    @Mapping(target = "timestamp", ignore = true)
    // Atualiza o 'userId' com o valor vindo do evento; se for nulo, mantém o 'userId' já gravado na entidade.
    @Mapping(target = "userId", expression = "java(event.getUserId() != null ? event.getUserId() : entity.getUserId())")
    // Atualiza o 'eventType' com o valor do evento em String; se for nulo, mantém o 'eventType' original da entidade.
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : entity.getEventType())")
    // Sobrescreve o campo 'timestampUpdate' com a data e hora exatas de quando a atualização parcial foi executada.
    @Mapping(target = "timestampUpdate", expression = "java(Instant.now())")
    // Aplica as alterações enviadas no DTO diretamente sobre uma instância já existente da entidade Activity (@MappingTarget).
    void updateEntityFromDto(ActivityEvent event, ActivityUpdateRequest dto, @MappingTarget Activity entity);
}

/*import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.model.ActivityLog;
import org.mapstruct.*;
import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.model.Activity;

import java.time.Instant;

// 1. Informa ao MapStruct que esta interface é um mapeador de objetos.
// 2. 'componentModel = "spring"' faz o Spring gerenciar essa classe como um Bean (@Component),
//    permitindo que você a injete via construtor no Consumer ou na Service.
@Mapper(componentModel = "spring", imports = {Instant.class, ActivityEvent.class})
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
    @Mapping(target = "timestamp", expression = "java(Instant.now())")
    // Define o timestampUpdate atual no momento da conversão
    @Mapping(target = "timestampUpdate", expression = "java(Instant.now())")
    /* Assinatura do método de conversão:*/ // Recebe dois objetos de entrada (o Envelope do Kafka e o Payload do DTO). // e retorna a Entidade (Activity) totalmente preenchida para ser salva no MongoDB.
//    Activity activityEntity(ActivityEvent event, ActivityCreateRequest dto);
//
//    // 👈 NOVO: Mapeamento de Logs de Consulta (READ)
//    // O MapStruct mapeia automaticamente timestamp, totalRecordsConsulted e activities do 'dto'
//    // e extrai o userId e eventType do 'event'.
//    @Mapping(target = "id", ignore = true) // Deixa o Mongo gerar o ObjectId
//    @Mapping(target = "userId", source = "event.userId")
//    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : null)")
//    ActivityLog toActivityLog(ActivityEvent event, ActivityLog dto);
//
//    // 🔴 NOVO: Atualização Parcial (@MappingTarget)
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // 👈 Ignora campos nulos no DTO
//    @Mapping(target = "id", ignore = true)        // Preserva o ID original do MongoDB
//    @Mapping(target = "timestamp", ignore = true) // Preserva o timestamp original de CRIAÇÃO
//    @Mapping(target = "userId", expression = "java(event.getUserId() != null ? event.getUserId() : entity.getUserId())")
//    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : entity.getEventType())")
//    @Mapping(target = "timestampUpdate", expression = "java(Instant.now())") // 👈 Preenche SEMPRE a data atual do Update
//    void updateEntityFromDto(ActivityEvent event, ActivityUpdateRequest dto, @MappingTarget Activity entity);
//}
