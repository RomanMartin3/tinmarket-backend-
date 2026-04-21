package com.tinmarket.backend.controller;

import com.tinmarket.backend.model.Proveedor;
import com.tinmarket.backend.service.ProveedorService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public ResponseEntity<List<Proveedor>> listar(@RequestParam Long negocioId) {
        return ResponseEntity.ok(proveedorService.listarPorNegocio(negocioId));
    }

    @PostMapping
    public ResponseEntity<Proveedor> guardar(@RequestBody Proveedor proveedor, @RequestParam Long negocioId) {
        return ResponseEntity.ok(proveedorService.guardar(proveedor, negocioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            proveedorService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("No se puede eliminar este proveedor porque tiene productos asignados.");
        }
    }
}