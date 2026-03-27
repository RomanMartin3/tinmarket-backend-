package com.tinmarket.backend.controller;

import com.tinmarket.backend.model.Categoria;
import com.tinmarket.backend.service.CategoriaService;
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
    public ResponseEntity<Categoria> crear(@RequestBody Categoria categoria, @RequestParam Long negocioId) {
        return ResponseEntity.ok(categoriaService.crearCategoria(categoria, negocioId));
    }
}