package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.NuevaVentaRequestDTO;
import com.tinmarket.backend.dto.VentaResponseDTO;
import com.tinmarket.backend.security.SecurityUtils;
import com.tinmarket.backend.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*") // Permite peticiones desde React
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // 1. Crear Venta (Cajero cobra)
    // POST /api/ventas
    @PostMapping
    public ResponseEntity<VentaResponseDTO> crearVenta(@RequestBody @Valid NuevaVentaRequestDTO dto) {
        dto.setNegocioId(SecurityUtils.getNegocioId()); // Aislamiento garantizado al facturar
        VentaResponseDTO response = ventaService.crearVenta(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        Long negocioId = SecurityUtils.getNegocioId();
        List<VentaResponseDTO> ventas = ventaService.listarVentas(negocioId, desde, hasta);
        return ResponseEntity.ok(ventas);
    }

    // 4. Anular Venta (Devolución)
    // POST /api/ventas/123/anular
    @PostMapping("/{id}/anular")
    public ResponseEntity<Void> anularVenta(@PathVariable Long id) {
        ventaService.anularVenta(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content (Éxito sin body)
    }
}