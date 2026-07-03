package com.exemple.activity.model;

import com.exemple.activity.dto.ActivityResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "activity_logs") // Collection separada no MongoDB
@Data
public class ActivityLog {
    @Id
    private String id;
    private LocalDateTime queryTimestamp;
    private int totalRecordsConsulted;
    private List<ActivityResponse> activities;
}
