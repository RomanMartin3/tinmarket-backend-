package com.tinmarket.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoRequestDTO {

    // Si viene ID es una edición, si es null es creación
    private Long id;

    @NotNull(message = "El negocio es obligatorio")
    private Long negocioId;

    private Long categoriaId;
    private Long proveedorId;

    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull @PositiveOrZero
    private BigDecimal costoActual;

    @NotNull @PositiveOrZero
    private BigDecimal margenGanancia;

    private BigDecimal tasaIva; // Opcional, default 0 en logica

    // El precio de venta se puede calcular solo, pero a veces el usuario quiere forzarlo
    private BigDecimal precioVentaActual;

    @NotNull
    private BigDecimal stockActual;

    private BigDecimal stockMinimoAlerta;

    private String unidadMedida; // ENUM como String

    // LISTA DE CÓDIGOS DE BARRA (Para cargar Packs y Unidades juntos)
    private List<CodigoBarraDTO> codigosBarra;
}