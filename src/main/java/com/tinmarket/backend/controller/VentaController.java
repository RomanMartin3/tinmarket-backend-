package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.NuevaVentaRequestDTO;
import com.tinmarket.backend.dto.VentaResponseDTO;
import com.tinmarket.backend.model.Venta;
import com.tinmarket.backend.security.SecurityUtils;
import com.tinmarket.backend.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<Venta>> listarVentas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long negocioId = SecurityUtils.getNegocioId();
        Pageable pageable = PageRequest.of(page, size);

        // Cambiamos de obtenerVentasDelDia a obtenerVentasPaginadas
        return ResponseEntity.ok(ventaService.obtenerVentasPaginadas(negocioId, pageable));
    }

    // 4. Anular Venta (Devolución)
    // POST /api/ventas/123/anular
    @PostMapping("/{id}/anular")
    public ResponseEntity<Void> anularVenta(@PathVariable Long id) {
        ventaService.anularVenta(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content (Éxito sin body)
    }
}