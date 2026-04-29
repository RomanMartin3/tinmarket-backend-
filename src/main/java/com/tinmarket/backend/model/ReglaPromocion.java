package com.tinmarket.backend.model;

import com.tinmarket.backend.model.enums.TipoDescuento;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "reglas_promocion")
public class ReglaPromocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "promocion_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Promocion promocion;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Tu campo original (lo inicializamos en 1 por defecto para que las promos simples funcionen desde la primera unidad)
    @Column(name = "cantidad_necesaria", nullable = false)
    private BigDecimal cantidadNecesaria = BigDecimal.ONE;

    // NUEVOS CAMPOS: Para que el servicio pueda guardar el descuento
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento;

    @Column(name = "valor_descuento", nullable = false)
    private BigDecimal valorDescuento;
}