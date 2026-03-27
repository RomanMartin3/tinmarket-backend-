package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemVentaRequestDTO {
    // El cajero manda el código de barras (puede ser de pack o unidad)
    private String codigoBarraEscaneado;

    // O manda el ID si lo buscó manual por nombre
    private Long productoId;

    private BigDecimal cantidad;
}