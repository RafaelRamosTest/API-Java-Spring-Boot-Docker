
package com.exemple.activity.controller;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/activities")
//@PreAuthorize("isAuthenticated()")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityResponse> getActivities(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {
        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        String route = request.getMethod() + " " + request.getRequestURI(); // Pega o méto-do e o caminho real chamado (ex: "GET /activities")
        return activityService.listAllActivitiesAndLog(userId, route);
    }

    @PostMapping("/create")
    public ActivityCreateRequest createActivity(
            @RequestBody ActivityCreateRequest activity,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        String route = request.getMethod() + " " + request.getRequestURI(); // Pega o méto-do e o caminho real chamado (ex: "POST /create")
        return activityService.createActivity(activity, userId, route);
    }

    @PutMapping("/update/{id}")
    public ActivityUpdateRequest updateActivity(
            @PathVariable String id,
            @RequestBody ActivityUpdateRequest activity,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        String route = request.getMethod() + " " + request.getRequestURI(); // Pega o méto-do e o caminho real chamado (ex: "PUT /update/{id}")
        return activityService.updateActivity(id, activity, userId, route);
    }
}