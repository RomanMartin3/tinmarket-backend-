package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.ProductoRequestDTO;
import com.tinmarket.backend.dto.ProductoResponseDTO;
import com.tinmarket.backend.model.Producto;
import com.tinmarket.backend.repository.ProductoRepository;
import com.tinmarket.backend.security.SecurityUtils;
import com.tinmarket.backend.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<Producto>> listarTodos(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        Long negocioId = SecurityUtils.getNegocioId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productos = productoService.listarActivosPorNegocio(negocioId, pageable);


        return ResponseEntity.ok(productos);
    }

    // 1. Crear o Editar Producto
    // POST http://localhost:8080/api/productos
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> guardarProducto(@RequestBody @Valid ProductoRequestDTO dto) {
        dto.setNegocioId(SecurityUtils.getNegocioId());
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
            @RequestParam String query) {

        Long negocioId = SecurityUtils.getNegocioId();
        List<Producto> resultados = productoRepository.buscarEnPOS(query, negocioId);
        return ResponseEntity.ok(resultados);
    }


}