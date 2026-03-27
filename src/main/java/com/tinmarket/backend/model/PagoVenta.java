package com.tinmarket.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.tinmarket.backend.model.enums.TipoPago;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "pagos_venta")
public class PagoVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonBackReference // Evita bucle infinito en JSON
    @ToString.Exclude
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago")
    private TipoPago tipoPago;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(name = "referencia_pago")
    private String referenciaPago;
}
