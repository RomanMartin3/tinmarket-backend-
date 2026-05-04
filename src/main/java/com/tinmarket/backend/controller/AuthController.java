package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.AuthResponseDTO;
import com.tinmarket.backend.dto.LoginRequestDTO;
import com.tinmarket.backend.model.Usuario;
import com.tinmarket.backend.repository.UsuarioRepository;
import com.tinmarket.backend.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO request) {
        try {
            // Spring Security verifica la contraseña automáticamente
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
            String jwt = jwtService.generarToken(usuario);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(jwt);
            response.setNombreUsuario(usuario.getNombreCompleto());
            response.setRol(usuario.getRol().name());
            response.setNegocioId(usuario.getNegocio().getId());

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }
}