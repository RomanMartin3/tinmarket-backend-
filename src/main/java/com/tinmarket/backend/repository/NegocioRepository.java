package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    // Lógica: Evitar que dos negocios se registren con el mismo CUIT
    boolean existsByCuit(String cuit);

    // Lógica: Buscar datos para facturación
    Optional<Negocio> findByCuit(String cuit);
}
