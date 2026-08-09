package com.exemple.activity.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

@Document(collection = "activities")
@Data
public class Activity {
    @Id
    @Field(targetType = FieldType.STRING)
    private String id;
    private String title;
    private Boolean completed;
    private String eventType;
    private String userId;
    private Instant timestamp;
    private Instant timestampUpdate;
}
