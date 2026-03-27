package com.tinmarket.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class NuevaVentaRequestDTO {

    @NotNull
    private Long negocioId;

    @NotNull
    private Long sesionCajaId; // A qué caja asignamos la plata

    private Long clienteId; // Opcional

    @NotEmpty(message = "La venta debe tener al menos un producto")
    private List<ItemVentaRequestDTO> items;

    @NotEmpty(message = "Debe haber al menos un método de pago")
    private List<PagoVentaDTO> pagos;

    // Validación de seguridad para el frontend: El total que calculó el JS
    // Lo comparamos con el Backend por si hubo manipulación.
    private BigDecimal totalEsperado;
}