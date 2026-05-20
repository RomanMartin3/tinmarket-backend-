package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.PagoCuentaCorriente;
import com.tinmarket.backend.model.enums.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoCuentaCorrienteRepository extends JpaRepository<PagoCuentaCorriente, Long> {
    List<PagoCuentaCorriente> findBySesionCajaIdAndTipoPago(Long sesionCajaId, TipoPago tipoPago);
}