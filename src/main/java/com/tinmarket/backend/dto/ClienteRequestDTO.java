package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClienteRequestDTO {
    private String nombre;
    private String telefono;
    private BigDecimal limiteCredito;
}