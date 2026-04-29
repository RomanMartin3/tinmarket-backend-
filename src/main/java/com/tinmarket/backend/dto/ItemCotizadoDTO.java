package com.tinmarket.backend.dto;

import com.tinmarket.backend.model.Producto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCotizadoDTO {
    private Producto producto;

    // Lo que pidió el cliente (Ej: 2 combos)
    private BigDecimal cantidadSolicitada;

    // Lo que realmente se descuenta de stock físico (por códigos de barra de peso/bulto)
    private BigDecimal cantidadFisicaTotal;

    // "Pool" de items para el motor de promociones. Se va restando a medida que un combo lo usa.
    private BigDecimal cantidadDisponibleParaPromos;

    // Acumuladores de plata
    private BigDecimal descuentoTotal = BigDecimal.ZERO;
    private BigDecimal subtotalFinal = BigDecimal.ZERO;

    public ItemCotizadoDTO(Producto producto, BigDecimal cantidadSolicitada, BigDecimal cantidadFisicaTotal) {
        this.producto = producto;
        this.cantidadSolicitada = cantidadSolicitada;
        this.cantidadFisicaTotal = cantidadFisicaTotal;
        // Al iniciar, todos los items están disponibles para aplicarles promociones
        this.cantidadDisponibleParaPromos = cantidadSolicitada;
    }
}