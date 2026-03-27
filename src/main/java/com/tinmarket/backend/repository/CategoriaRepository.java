package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Lógica: Traer todas las categorías de UN solo negocio (para el combo desplegable del frontend)
    List<Categoria> findByNegocioId(Long negocioId);

    // Opcional: Para evitar duplicados de nombre en el mismo negocio
    boolean existsByNombreAndNegocioId(String nombre, Long negocioId);
}