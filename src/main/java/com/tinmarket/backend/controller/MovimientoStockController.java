package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.MovimientoStockRequestDTO;
import com.tinmarket.backend.model.MovimientoStock;
import com.tinmarket.backend.service.MovimientoStockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos-stock")
@CrossOrigin(origins = "*") // Esto evita el error de CORS / Failed to fetch
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    public MovimientoStockController(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @GetMapping
    public ResponseEntity<List<MovimientoStock>> listar(@RequestParam Long negocioId) {
        return ResponseEntity.ok(movimientoStockService.listarPorNegocio(negocioId));
    }

    @PostMapping
    public ResponseEntity<MovimientoStock> registrar(@RequestBody @Valid MovimientoStockRequestDTO dto) {
        return ResponseEntity.ok(movimientoStockService.registrarMovimientoManual(dto));
    }
}