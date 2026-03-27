package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Lógica: "Dame todas las ventas de este mes para el gráfico de ganancias".
    // Spring traduce 'Between' a un rango de fechas SQL.
    List<Venta> findByNegocioIdAndFechaVentaBetween(Long negocioId, LocalDateTime inicio, LocalDateTime fin);

    // Lógica: Ver las ventas de una sesión de caja específica (ej: para el arqueo de caja del turno tarde).
    List<Venta> findBySesionCajaId(Long sesionCajaId);
}