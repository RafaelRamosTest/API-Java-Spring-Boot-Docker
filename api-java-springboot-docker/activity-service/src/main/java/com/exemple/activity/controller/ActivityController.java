package com.exemple.activity.controller;

import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.service.ActivityService;
import com.exemple.activity.service.ActivityProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityProducer activityProducer;

    public ActivityController(ActivityService activityService, ActivityProducer activityProducer) {
        this.activityService = activityService;
        this.activityProducer = activityProducer;
    }

    @GetMapping("/activities")
    public List<ActivityResponse> getActivities() throws JsonProcessingException {
        List<ActivityResponse> activities = activityService.listAllActivities();

        // Converte a lista em JSON
        ObjectMapper mapper = new ObjectMapper();
        String activitiesJson = mapper.writeValueAsString(activities);

        // Publica o JSON no Kafka
        activityProducer.publishActivity(activitiesJson);

        return activities;
    }

}
