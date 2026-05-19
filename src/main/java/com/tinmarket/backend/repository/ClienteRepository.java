package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNegocioIdAndNombreContainingIgnoreCase(Long negocioId, String nombre);
}