package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String sku;
    private String categoriaNombre; // Solo el nombre, no el objeto entero
    private String proveedorNombre;
    private BigDecimal precioVenta;
    private BigDecimal stockActual;
    private String unidadMedida;
    private List<CodigoBarraDTO> codigos; // Para que el usuario vea qué códigos tiene asociados
}