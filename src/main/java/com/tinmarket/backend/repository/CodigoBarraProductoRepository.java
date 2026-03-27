package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.CodigoBarraId;
import com.tinmarket.backend.model.CodigoBarraProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodigoBarraProductoRepository extends JpaRepository<CodigoBarraProducto, CodigoBarraId> {

    // Lógica: El cajero escanea "7791234".
    // El sistema busca ese código ESPECÍFICAMENTE en este negocio.
    // Devuelve el objeto que dice "Esto es una Coca Cola y descuenta 1 unidad".
    Optional<CodigoBarraProducto> findByCodigoBarraAndNegocioId(String codigoBarra, Long negocioId);

    // Lógica: Ver todos los códigos asociados a un producto (ej: ver si la Coca tiene cargado el pack y la unidad)
    List<CodigoBarraProducto> findByProductoId(Long productoId);
}