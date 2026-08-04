
package com.exemple.activity.controller;

import com.exemple.activity.dto.ActivityCreateRequest;
import com.exemple.activity.dto.ActivityResponse;
import com.exemple.activity.service.ActivityService;
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
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        return activityService.listAllActivitiesAndLog(userId);
    }

    @PostMapping("/create")
    public ActivityCreateRequest createActivity(
            @RequestBody ActivityCreateRequest activity,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // Captura o ID do usuário logado
        return activityService.createActivity(activity, userId);
    }
}