package com.tinmarket.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromocionRequestDTO {
    @NotNull
    private Long negocioId;

    @NotNull
    private String nombre;

    @NotNull
    private LocalDateTime fechaInicio;

    @NotNull
    private LocalDateTime fechaFin;

    @NotEmpty(message = "Debe seleccionar al menos un producto")
    private List<Long> productoIds;

    @NotNull
    private String tipoDescuento; // PORCENTAJE, MONTO_FIJO, N_X_M (Ej: 2x1)

    @NotNull
    private BigDecimal valorDescuento; // Ej: 15 (%), 500 ($), o 2 (si es 2x1)
}