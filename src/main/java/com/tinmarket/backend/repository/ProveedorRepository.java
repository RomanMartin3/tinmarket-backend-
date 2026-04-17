package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByNegocioId(Long negocioId);
}
