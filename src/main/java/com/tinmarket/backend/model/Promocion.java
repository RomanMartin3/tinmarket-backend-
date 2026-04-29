package com.tinmarket.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import com.tinmarket.backend.model.enums.TipoDescuento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "promociones")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento")
    private TipoDescuento tipoDescuento;

    @Column(name = "valor_descuento", nullable = false)
    private BigDecimal valorDescuento;


    private boolean activa;

    @OneToMany(mappedBy = "promocion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("promocion")
    private List<ReglaPromocion> reglas;
}
