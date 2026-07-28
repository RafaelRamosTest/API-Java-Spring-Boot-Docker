package com.exemple.activity.dto;

import lombok.Data;

@Data
public class ActivityEvent {
    private String eventType; // "ACTIVITY_CREATED", "ACTIVITY_UPDATED", etc.
    private ActivityUpdateRequest payload;
}
