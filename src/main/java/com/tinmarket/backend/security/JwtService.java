package com.tinmarket.backend.security;

import com.tinmarket.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Llave secreta (En producción esto debe ir en el application.properties o variables de entorno)
    // Es una llave generada en Base64 estática para este proyecto
    @Value("${jwt.secret}")
    private String secretKey;

    public String generarToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Inyectamos el ID del negocio en el token. ¡Esta es la clave del Multi-Tenant!
        extraClaims.put("negocioId", usuario.getNegocio().getId());
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("usuarioId", usuario.getId());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(usuario.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 horas
                .signWith(getSignInKey())
                .compact();
    }

    public String extraerEmail(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public Long extraerNegocioId(String token) {
        return extraerClaim(token, claims -> claims.get("negocioId", Long.class));
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        final String email = extraerEmail(token);
        return (email.equals(userDetails.getUsername())) && !esTokenExpirado(token);
    }

    private boolean esTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}