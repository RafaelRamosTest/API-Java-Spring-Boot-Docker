package com.exemple.activity.service;

import com.exemple.activity.dto.ActivityResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ActivityService {

    private static final String url = "https://fakerestapi.azurewebsites.net/api/v1/Activities";

    private final RestTemplate restTemplate = new RestTemplate();
    //@Cacheable("activities")
    public List<ActivityResponse> listAllActivities() {
        ActivityResponse[] response = restTemplate.getForObject(url, ActivityResponse[].class);
        return Arrays.asList(response);
    }
}
