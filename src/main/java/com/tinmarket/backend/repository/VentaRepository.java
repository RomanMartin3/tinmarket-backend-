package com.tinmarket.backend.repository;

import com.tinmarket.backend.dto.ReportesDTO.*;
import com.tinmarket.backend.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByNegocioIdAndFechaVentaBetween(Long negocioId, LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findBySesionCajaId(Long sesionCajaId);

    // --- NUEVAS QUERIES ANALÍTICAS PARA EL DASHBOARD ---

    // 1. KPIs Generales (El promedio se calcula ahora en el DTO)
    @Query("SELECT new com.tinmarket.backend.dto.ReportesDTO$KpiDTO(" +
            "COALESCE(SUM(v.totalVenta), 0), " +
            "COALESCE(SUM(d.subTotal - (d.costoHistorico * d.cantidadFisica)), 0), " +
            "COUNT(DISTINCT v.id)) " +
            "FROM DetalleVenta d JOIN d.venta v " +
            "WHERE v.negocio.id = :negocioId AND v.fechaVenta BETWEEN :desde AND :hasta AND v.estado = 'COMPLETADA'")
    KpiDTO obtenerKPIs(@Param("negocioId") Long negocioId, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // 2. Top Productos Vendidos
    @Query("SELECT new com.tinmarket.backend.dto.ReportesDTO$TopProductoDTO(" +
            "d.nombreProductoHistorico, SUM(d.cantidad), SUM(d.subTotal)) " +
            "FROM DetalleVenta d JOIN d.venta v " +
            "WHERE v.negocio.id = :negocioId AND v.fechaVenta BETWEEN :desde AND :hasta AND v.estado = 'COMPLETADA' " +
            "GROUP BY d.nombreProductoHistorico " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<TopProductoDTO> obtenerTopProductos(@Param("negocioId") Long negocioId, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // 3. Ventas por Medio de Pago
    @Query("SELECT new com.tinmarket.backend.dto.ReportesDTO$MedioPagoDTO(" +
            "CAST(p.tipoPago AS string), SUM(p.monto)) " +
            "FROM PagoVenta p JOIN p.venta v " +
            "WHERE v.negocio.id = :negocioId AND v.fechaVenta BETWEEN :desde AND :hasta AND v.estado = 'COMPLETADA' " +
            "GROUP BY p.tipoPago")
    List<MedioPagoDTO> obtenerVentasPorMedioPago(@Param("negocioId") Long negocioId, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    Page<Venta> findByNegocioIdOrderByFechaVentaDesc(Long negocioId, Pageable pageable);
}