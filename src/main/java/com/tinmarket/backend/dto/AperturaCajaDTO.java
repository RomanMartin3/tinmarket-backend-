package com.tinmarket.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AperturaCajaDTO {
    @NotNull
    private Long usuarioId;
    @NotNull
    private Long negocioId;
    @NotNull @PositiveOrZero
    private BigDecimal montoInicial;
}