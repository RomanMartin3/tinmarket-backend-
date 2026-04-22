package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleTicketDTO {
    private String nombreProducto;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subTotal;
}