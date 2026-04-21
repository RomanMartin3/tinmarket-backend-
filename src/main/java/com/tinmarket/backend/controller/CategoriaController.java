package com.tinmarket.backend.controller;

import com.tinmarket.backend.model.Categoria;
import com.tinmarket.backend.service.CategoriaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar(@RequestParam Long negocioId) {
        return ResponseEntity.ok(categoriaService.listarPorNegocio(negocioId));
    }

    @PostMapping
    public ResponseEntity<Categoria> guardar(@RequestBody Categoria categoria, @RequestParam Long negocioId) {
        return ResponseEntity.ok(categoriaService.guardar(categoria, negocioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            categoriaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Protección vital: Si la categoría tiene productos, la BD lanza esta excepción.
            return ResponseEntity.badRequest().body("No se puede eliminar esta categoría porque tiene productos asignados.");
        }
    }
}