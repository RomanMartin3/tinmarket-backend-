package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.ProductoRequestDTO;
import com.tinmarket.backend.dto.ProductoResponseDTO;
import com.tinmarket.backend.model.Producto;
import com.tinmarket.backend.repository.ProductoRepository;
import com.tinmarket.backend.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite que React (localhost:5173) se conecte sin bloqueos
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository; // Usamos el repo directo para lecturas simples (Performance)

    public ProductoController(ProductoService productoService, ProductoRepository productoRepository) {
        this.productoService = productoService;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos(@RequestParam Long negocioId) {

        List<Producto> productos = productoService.listarActivosPorNegocio(negocioId);
        return ResponseEntity.ok(productos);
    }

    // 1. Crear o Editar Producto
    // POST http://localhost:8080/api/productos
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> guardarProducto(@RequestBody @Valid ProductoRequestDTO dto) {
        ProductoResponseDTO response = productoService.crearEditarProducto(dto);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // 2. Buscador Tipo Google
    // GET http://localhost:8080/api/productos/search?query=coca&negocioId=1
    @GetMapping("/search")
    public ResponseEntity<List<Producto>> buscarProductos(
            @RequestParam String query,
            @RequestParam Long negocioId) {

        // Buscamos por nombre parcial
        List<Producto> resultados = productoRepository.buscarEnPOS(query, negocioId);
        return ResponseEntity.ok(resultados);
    }
    

}