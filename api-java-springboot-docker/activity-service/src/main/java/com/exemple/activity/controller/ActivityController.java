package com.exemple.activity.controller;

import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.service.ActivityService;
import com.exemple.activity.service.ActivityProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
@PreAuthorize("isAuthenticated()")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityProducer activityProducer;
    private final ObjectMapper mapper;

    public ActivityController(ActivityService activityService, ActivityProducer activityProducer, ObjectMapper mapper) {
        this.activityService = activityService;
        this.activityProducer = activityProducer;
        this.mapper = mapper;
    }

    @GetMapping
    public List<ActivityResponse> getActivities() throws JsonProcessingException {
        List<ActivityResponse> activities = activityService.listAllActivities();

        // Converte a lista em JSON usando o mapper injetado
        String activitiesJson = mapper.writeValueAsString(activities);

        // Publica o JSON no Kafka
        activityProducer.publishActivity(activitiesJson);

        return activities;
    }

    @PostMapping("/create")
    public ActivityResponse createActivity(
            @RequestBody ActivityResponse activity,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();//SERVE PARA REGISTRAR QUEM CADASTROU. COMO NÃO EXISTE BANCO DE DADOS, NÃO REGISTRA.
        System.out.println("Usuário " + userId + " está criando uma atividade.");

        return activityService.createActivity(activity);
    }
}