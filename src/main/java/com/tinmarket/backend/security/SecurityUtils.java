package com.tinmarket.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getNegocioId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UsuarioPrincipal) {
            return ((UsuarioPrincipal) principal).getNegocioId();
        }
        throw new RuntimeException("No se pudo obtener el negocio del contexto de seguridad");
    }

    public static Long getUsuarioId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UsuarioPrincipal) {
            return ((UsuarioPrincipal) principal).getUsuarioId();
        }
        throw new RuntimeException("No se pudo obtener el usuario del contexto de seguridad");
    }
}