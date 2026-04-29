package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.PromocionRequestDTO;
import com.tinmarket.backend.model.Promocion;
import com.tinmarket.backend.service.PromocionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promociones")
@CrossOrigin(origins = "*")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping
    public ResponseEntity<List<Promocion>> listar(@RequestParam Long negocioId) {
        return ResponseEntity.ok(promocionService.listarActivasPorNegocio(negocioId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody @Valid PromocionRequestDTO dto) {
        try {
            return ResponseEntity.ok(promocionService.crearPromocion(dto));
        } catch (IllegalArgumentException e) {
            // FIX: Atrapamos la rentabilidad negativa y devolvemos un JSON limpio (Error 400 en vez de 500)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        promocionService.desactivarPromocion(id);
        return ResponseEntity.noContent().build();
    }
}