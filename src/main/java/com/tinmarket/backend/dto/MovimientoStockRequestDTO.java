package com.tinmarket.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoStockRequestDTO {
    @NotNull
    private Long negocioId;

    @NotNull
    private Long productoId;

    @NotNull
    private Long usuarioId;

    @NotNull
    private String tipoMovimiento; // INGRESO, EGRESO, AJUSTE
    @NotNull
    private Boolean esIngreso;

    @NotNull
    private BigDecimal cantidad;

    private String motivo;
}