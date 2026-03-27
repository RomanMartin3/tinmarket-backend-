package com.tinmarket.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import com.tinmarket.backend.model.enums.EstadoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sesiones_caja")
public class SesionCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_inicial", nullable = false)
    private BigDecimal montoInicial;

    @Column(name = "monto_final_real")
    private BigDecimal montoFinalReal;

    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    private EstadoCaja estado;
}