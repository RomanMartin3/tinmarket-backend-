package com.tinmarket.backend.dto;

import com.tinmarket.backend.model.enums.TipoMovimientoCaja;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MovimientoCajaRequestDTO {
    private BigDecimal monto;
    private TipoMovimientoCaja tipoMovimiento;
    private String motivo;
}