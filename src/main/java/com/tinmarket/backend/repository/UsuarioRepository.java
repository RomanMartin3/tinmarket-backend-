package com.tinmarket.backend.repository;

import com.tinmarket.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Lógica: El login se hace por email. Devuelve Optional para manejar si no existe.
    Optional<Usuario> findByEmail(String email);

    // Lógica: Validar que no creemos dos usuarios con el mismo mail en el mismo negocio (o globalmente)
    boolean existsByEmail(String email);
}