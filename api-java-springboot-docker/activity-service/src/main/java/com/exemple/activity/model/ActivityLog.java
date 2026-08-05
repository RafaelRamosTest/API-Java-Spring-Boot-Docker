package com.exemple.activity.model;

import com.exemple.activity.dto.ActivityResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "activity_logs")
public class ActivityLog {

    @Id
    private String id;
    private String eventType;
    private String userId;
    private Instant timestamp;
    private int totalRecordsConsulted;
    private List<ActivityResponse> activities;
}
