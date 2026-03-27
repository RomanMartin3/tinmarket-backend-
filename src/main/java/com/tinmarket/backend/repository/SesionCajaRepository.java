package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.SesionCaja;
import com.tinmarket.backend.model.enums.EstadoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    // Lógica: Validar si este usuario YA tiene una caja abierta antes de dejarle abrir otra.
    // O recuperar la caja actual para asignarle la venta.
    Optional<SesionCaja> findByUsuarioIdAndEstado(Long usuarioId, EstadoCaja estado);
}