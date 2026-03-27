package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT DISTINCT p FROM Producto p " +
            "WHERE p.negocio.id = :negocioId AND p.activo = true " +
            "AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR p.sku = :query " +
            "OR p.id IN (SELECT c.producto.id FROM CodigoBarraProducto c WHERE c.codigoBarra = :query))")
    List<Producto> buscarEnPOS(@Param("query") String query, @Param("negocioId") Long negocioId);
}