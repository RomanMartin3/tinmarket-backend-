package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VentaResponseDTO {
    private Long id;
    private LocalDateTime fecha;
    private String nombreCajero;
    private BigDecimal total;
    private List<DetalleTicketDTO> items;
}