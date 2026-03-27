package com.tinmarket.backend.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token; // JWT
    private String nombreUsuario;
    private String rol;
    private Long negocioId;
}