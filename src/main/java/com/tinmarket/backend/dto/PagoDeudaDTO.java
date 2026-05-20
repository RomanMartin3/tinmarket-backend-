package com.tinmarket.backend.dto;

import com.tinmarket.backend.model.enums.TipoPago;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PagoDeudaDTO {
    private BigDecimal monto;
    private TipoPago medioPago;
}