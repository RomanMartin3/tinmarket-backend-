package com.tinmarket.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "codigos_barra_producto")
@IdClass(CodigoBarraId.class) // Vinculamos la clase ID
public class CodigoBarraProducto {

    @Id
    @Column(name = "codigo_barra")
    private String codigoBarra;

    @Id
    @ManyToOne
    @JoinColumn(name = "negocio_id")
    private Negocio negocio;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_a_descontar", nullable = false)
    private BigDecimal cantidadADescontar;
}