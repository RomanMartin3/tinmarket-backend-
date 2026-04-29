package com.tinmarket.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonBackReference // "Yo soy el inverso, NO me serialices de vuelta"
    @ToString.Exclude  // "Corta el bucle aquí"
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private BigDecimal cantidad;

    // SNAPSHOT HISTÓRICO
    @Column(name = "nombre_producto_historico", nullable = false)
    private String nombreProductoHistorico;

    @Column(name = "costo_historico", nullable = false)
    private BigDecimal costoHistorico;
    @Column(name = "cantidad_fisica", nullable = false)
    private BigDecimal cantidadFisica;

    @Column(name = "precio_unitario_historico", nullable = false)
    private BigDecimal precioUnitarioHistorico;

    @Column(name = "tasa_iva_historica")
    private BigDecimal tasaIvaHistorica;

    @Column(nullable = false)
    private BigDecimal subTotal;


}
