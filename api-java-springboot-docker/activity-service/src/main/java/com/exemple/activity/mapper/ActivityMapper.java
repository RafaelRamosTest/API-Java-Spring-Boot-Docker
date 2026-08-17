package com.exemple.activity.mapper;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityEvent;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.model.Activity;
import com.exemple.activity.model.ActivityLog;
import io.hypersistence.tsid.TSID;
import org.mapstruct.*;

import java.time.Instant;

// Indica ao MapStruct que é uma interface de mapeamento e faz o Spring gerenciá-la como Bean (@Component), além de importar as classes Instant, ActivityEvent e TSID para uso nas expressões Java.
@Mapper(componentModel = "spring", imports = {Instant.class, ActivityEvent.class, TSID.class})
public interface ActivityMapper {

    // 🔴 Garante que NUNCA sobrescreva: gera um novo TSID único para a chave primária do LOG
    @Mapping(target = "id", expression = "java(String.valueOf(TSID.fast().toLong()))")
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

    // Define o ID do log: utiliza o ID existente do dto ou gera um novo TSID de 64 bits caso seja nulo.
    @Mapping(target = "id", expression = "java(dto.getId() != null ? dto.getId() : String.valueOf(TSID.fast().toLong()))")
    // Extrai o 'userId' do evento Kafka e o insere no registro de log.
    @Mapping(target = "userId", source = "event.userId")
    // Converte o Enum do tipo de evento em String de forma segura para gravar no log.
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().name() : null)")
    // Garante a presença do timestamp original do dto ou atribui a data e hora atuais como fallback.
    @Mapping(target = "timestamp", expression = "java(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now())")
    // Mapeia a rota consultada a partir do dto de log.
    @Mapping(target = "route", source = "dto.route")
    // Mapeia a quantidade total de registros consultados.
    @Mapping(target = "totalRecordsConsulted", source = "dto.totalRecordsConsulted")
    // Mapeia a lista de atividades obtidas na consulta.
    @Mapping(target = "activities", source = "dto.activities")
    // Mapeia o código de status HTTP (ex: 200, 404, 500) vindo do dto de log.
    @Mapping(target = "statusCode", source = "dto.statusCode")
    // Mapeia o status da operação ("SUCCESS" ou "ERROR").
    @Mapping(target = "status", source = "dto.status")
    // Mapeia a mensagem detalhada de erro em caso de falha de integração ou execução.
    @Mapping(target = "errorMessage", source = "dto.errorMessage")
    // 🔴 Mapeia explicitamente a propriedade payload para resolver a ambiguidade
    @Mapping(target = "payload", source = "dto.payload")
    // Converte os dados do evento do Kafka e o objeto ActivityLog de origem em um novo documento ActivityLog completo.
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
