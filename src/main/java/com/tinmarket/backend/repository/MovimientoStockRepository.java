package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.MovimientoStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    // Lógica: "Ver el historial de movimientos de la Coca Cola"
    // Ideal para saber si hubo robos o mermas.
    Page<MovimientoStock> findByProductoIdOrderByFechaDesc(Long productoId, Pageable pageable);

    // Lógica: Ver todos los movimientos de un negocio
    Page<MovimientoStock> findByNegocioIdOrderByFechaDesc(Long negocioId, Pageable pageable);
}