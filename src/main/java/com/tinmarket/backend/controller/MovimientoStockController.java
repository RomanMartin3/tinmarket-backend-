package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.MovimientoStockRequestDTO;
import com.tinmarket.backend.model.MovimientoStock;
import com.tinmarket.backend.security.SecurityUtils;
import com.tinmarket.backend.service.MovimientoStockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<MovimientoStock>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 1. Obtenemos el negocio del usuario logueado
        Long negocioId = SecurityUtils.getNegocioId();

        // 2. Armamos la página (sin Sort porque el Repositorio ya lo ordena)
        Pageable pageable = PageRequest.of(page, size);

        // 3. Devolvemos la página segura
        Page<MovimientoStock> movimientos = movimientoStockService.listarPorNegocio(negocioId, pageable);
        return ResponseEntity.ok(movimientos);
    }

    @PostMapping
    public ResponseEntity<MovimientoStock> registrar(@RequestBody @Valid MovimientoStockRequestDTO dto) {
        dto.setNegocioId(SecurityUtils.getNegocioId());
        dto.setUsuarioId(SecurityUtils.getUsuarioId());
        return ResponseEntity.ok(movimientoStockService.registrarMovimientoManual(dto));
    }
}