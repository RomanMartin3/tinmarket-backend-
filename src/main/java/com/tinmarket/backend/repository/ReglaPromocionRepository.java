package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.ReglaPromocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReglaPromocionRepository extends JpaRepository<ReglaPromocion, Long> {
    @Query("SELECT r FROM ReglaPromocion r WHERE r.producto.id = :productoId AND r.promocion.activa = true AND r.promocion.fechaInicio <= CURRENT_TIMESTAMP AND r.promocion.fechaFin >= CURRENT_TIMESTAMP")
    List<ReglaPromocion> findActiveRulesByProducto(@Param("productoId") Long productoId);
}