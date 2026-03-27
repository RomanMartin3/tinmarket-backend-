package com.tinmarket.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.tinmarket.backend.model.enums.EstadoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @ManyToOne
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    private SesionCaja sesionCaja;

    // Dejamos clienteId como Long simple por ahora (opcional)
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_descuentos")
    private BigDecimal totalDescuentos;

    @Column(name = "total_venta", nullable = false)
    private BigDecimal totalVenta;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // <--- OBLIGATORIO: Evita el bucle infinito al imprimir logs
    @JsonManagedReference // <--- OBLIGATORIO: Evita bucle infinito al convertir a JSON
    private List<DetalleVenta> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonManagedReference
    private List<PagoVenta> pagos = new ArrayList<>();

    // "Helper Method": Buena práctica para mantener la coherencia
    // Cuando agregas un detalle, este método se asegura de asignar la venta al detalle
    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}