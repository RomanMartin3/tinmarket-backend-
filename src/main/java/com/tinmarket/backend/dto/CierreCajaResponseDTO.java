package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CierreCajaResponseDTO {
    private Long sesionId;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal totalVentasEfectivo;
    private BigDecimal montoEsperado; // Lo que el sistema calculó
    private BigDecimal montoFinalInformado; // Lo que el cajero contó
    private BigDecimal diferencia;
    private String estado;
}