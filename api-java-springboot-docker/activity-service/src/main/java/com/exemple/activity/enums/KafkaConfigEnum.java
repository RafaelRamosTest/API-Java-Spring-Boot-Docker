package com.exemple.activity.enums;
public enum KafkaConfigEnum {

    ATIVIDADES("activities-topic", "activity-group", "Fluxo de criação de atividades"),
    LOGS("activities-log-topic", "activity-group", "Fluxo de telemetria e logs de consultas");

    private final String topic;
    private final String groupId;
    private final String description;

    // Construtor do Enum
    KafkaConfigEnum(String topic, String groupId, String description) {
        this.topic = topic;
        this.groupId = groupId;
        this.description = description;
    }

    // Getters para acessar os valores no código
    public String getTopic() {
        return topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getDescription() {
        return description;
    }
}
