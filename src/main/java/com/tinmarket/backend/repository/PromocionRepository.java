package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Promocion;
import com.tinmarket.backend.model.ReglaPromocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByNegocioIdAndActivaTrue(Long negocioId);
    @Query("SELECT DISTINCT p FROM Promocion p JOIN p.reglas r WHERE p.negocio.id = :negocioId AND p.activa = true AND r.producto.id IN :productoIds")
    List<Promocion> findActivasPorNegocioYProductos(@Param("negocioId") Long negocioId, @Param("productoIds") Set<Long> productoIds);
}