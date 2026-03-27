package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PagoVentaDTO {
    private String tipoPago; // EFECTIVO, MERCADOPAGO, ETC.
    private BigDecimal monto;
    private String referencia; // Nro de cupón o transacción MP
}