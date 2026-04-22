package com.tinmarket.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import com.tinmarket.backend.model.enums.UnidadMedida;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "negocio_id") // Puede ser null si es catálogo global
    private Negocio negocio;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    private String sku;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    // PRECIOS (Siempre BigDecimal)
    @Column(name = "costo_actual", nullable = false)
    private BigDecimal costoActual;

    @Column(name = "margen_ganancia", nullable = false)
    private BigDecimal margenGanancia;

    @Column(name = "tasa_iva")
    private BigDecimal tasaIva;

    @Column(name = "precio_venta_actual", nullable = false)
    private BigDecimal precioVentaActual;

    // STOCK
    @Column(name = "stock_actual", nullable = false)
    private BigDecimal stockActual;

    @Column(name = "stock_minimo_alerta")
    private BigDecimal stockMinimoAlerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida")
    private UnidadMedida unidadMedida;

    // CONCURRENCIA (Optimistic Locking)
    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long version = 0L;

    private Boolean activo;

    @Column(name = "es_catalogo_global")
    private Boolean esCatalogoGlobal;
}