package com.exemple.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private String id;
    private String name;
    private String description;
}

/*
@Data: Substituiu todos os getters, setters, toString(), equals() e hashCode().

@NoArgsConstructor: Cria o construtor padrão sem argumentos (public ActivityCreateRequest() {}), necessário para frameworks como Jackson (serialização JSON) e Spring.

@AllArgsConstructor: Cria o construtor com todos os parâmetros que você tinha definido manualmente.
*/