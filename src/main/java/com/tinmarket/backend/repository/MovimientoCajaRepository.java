package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.MovimientoCaja;
import com.tinmarket.backend.model.enums.TipoMovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findBySesionCajaIdAndTipoMovimiento(Long sesionCajaId, TipoMovimientoCaja tipoMovimiento);
    List<MovimientoCaja> findBySesionCajaIdOrderByFechaMovimientoDesc(Long sesionCajaId);
}