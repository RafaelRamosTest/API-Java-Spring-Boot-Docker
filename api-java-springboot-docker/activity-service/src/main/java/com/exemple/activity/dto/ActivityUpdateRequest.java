package com.exemple.activity.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateRequest {
    @Size(min = 5, max = 250, message = "O título deve ter entre 1 e 250 caracteres")
    private String title;
    private Boolean completed;
}
/*
@Data: Substituiu todos os getters, setters, toString(), equals() e hashCode().

@NoArgsConstructor: Cria o construtor padrão sem argumentos (public ActivityUpdateRequest() {}), necessário para frameworks como Jackson (serialização JSON) e Spring.

@AllArgsConstructor: Cria o construtor com todos os parâmetros que você tinha definido manualmente.
*/