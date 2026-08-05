package com.exemple.activity.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.Instant;

@Document(collection = "activities")
@Data
public class Activity {
    @Id
    private String id;
    private String title;
    private Boolean completed;
    private String eventType;
    private String userId;
    private Instant timestamp;
}
