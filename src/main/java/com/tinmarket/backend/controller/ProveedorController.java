package com.tinmarket.backend.controller;

import com.tinmarket.backend.model.Proveedor;
import com.tinmarket.backend.repository.ProveedorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    public ProveedorController(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @GetMapping
    public ResponseEntity<List<Proveedor>> listar(@RequestParam Long negocioId) {
        return ResponseEntity.ok(proveedorRepository.findByNegocioId(negocioId));
    }
}