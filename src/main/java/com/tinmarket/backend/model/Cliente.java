package com.tinmarket.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Column(name = "saldo_deuda", nullable = false)
    private BigDecimal saldoDeuda = BigDecimal.ZERO;

    @Column(name = "limite_credito", nullable = false)
    private BigDecimal limiteCredito = new BigDecimal("50000.00");

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;
}