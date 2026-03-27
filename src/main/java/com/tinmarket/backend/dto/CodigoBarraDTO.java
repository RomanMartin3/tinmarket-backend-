package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CodigoBarraDTO {
    private String codigo;
    private BigDecimal cantidadADescontar; // 1 para unidad, 6 para pack, etc.
}