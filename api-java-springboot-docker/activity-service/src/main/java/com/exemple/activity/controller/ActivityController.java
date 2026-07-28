
package com.exemple.activity.controller;

import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.dto.ActivityUpdateRequest;
import com.exemple.activity.service.ActivityService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public List<ActivityResponse> getActivities() {
        return activityService.listAllActivitiesAndLog();
    }

    @PostMapping("/create")
    public ActivityResponse createActivity(
            @RequestBody ActivityResponse activity,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        return activityService.createActivity(activity, userId);
    }

    @PutMapping("/update/{idActivity}")
    public ActivityUpdateRequest updateActivity(
            @RequestBody ActivityUpdateRequest activity,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String idActivity) {

        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        return activityService.updateActivity(activity, userId, idActivity);
    }
}