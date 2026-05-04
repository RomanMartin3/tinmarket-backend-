package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.PromocionRequestDTO;
import com.tinmarket.backend.model.Promocion;
import com.tinmarket.backend.security.SecurityUtils;
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
    public ResponseEntity<List<Promocion>> listar() {
        Long negocioId = SecurityUtils.getNegocioId();
        return ResponseEntity.ok(promocionService.listarTodasPorNegocio(negocioId));
    }
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long id) {
        promocionService.alternarEstadoPromocion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody @Valid PromocionRequestDTO dto) {
        try {
            dto.setNegocioId(SecurityUtils.getNegocioId()); // Seguridad: Sobrescritura
            return ResponseEntity.ok(promocionService.crearPromocion(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promocionService.eliminarDefinitivamente(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody @Valid PromocionRequestDTO dto) {
        try {
            dto.setNegocioId(SecurityUtils.getNegocioId()); // Seguridad: Sobrescritura
            return ResponseEntity.ok(promocionService.editarPromocion(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}