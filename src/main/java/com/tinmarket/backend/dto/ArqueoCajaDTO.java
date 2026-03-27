package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ArqueoCajaDTO {
    private Long sesionId;
    private BigDecimal montoFinalInformado; // Lo que el cajero contó
    // El backend calculará la diferencia
}