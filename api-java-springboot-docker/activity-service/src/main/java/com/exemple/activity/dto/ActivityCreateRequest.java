package com.exemple.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivityCreateRequest(
        String id, // Opcional no JSON de entrada (gerado via TSID se nulo)

        @NotBlank(message = "O título é obrigatório")
        String title,

        @NotNull(message = "O status 'completed' é obrigatório")
        Boolean completed
) {}
