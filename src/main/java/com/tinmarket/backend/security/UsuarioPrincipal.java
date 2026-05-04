package com.tinmarket.backend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class UsuarioPrincipal extends User {
    private final Long negocioId;
    private final Long usuarioId;

    public UsuarioPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities, Long negocioId, Long usuarioId) {
        super(username, password, authorities);
        this.negocioId = negocioId;
        this.usuarioId = usuarioId;
    }

    public Long getNegocioId() {
        return negocioId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}