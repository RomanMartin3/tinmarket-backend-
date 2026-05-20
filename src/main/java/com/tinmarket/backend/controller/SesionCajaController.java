package com.tinmarket.backend.controller;


import com.tinmarket.backend.dto.AperturaCajaDTO;
import com.tinmarket.backend.dto.ArqueoCajaDTO;
import com.tinmarket.backend.dto.CierreCajaResponseDTO;
import com.tinmarket.backend.model.SesionCaja;
import com.tinmarket.backend.security.SecurityUtils;
import com.tinmarket.backend.service.SesionCajaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cajas")
@CrossOrigin(origins = "*")
public class SesionCajaController {

    private final SesionCajaService sesionCajaService;

    public SesionCajaController(SesionCajaService sesionCajaService) {
        this.sesionCajaService = sesionCajaService;
    }

    @PostMapping("/abrir")
    public ResponseEntity<SesionCaja> abrirCaja(@RequestBody @Valid AperturaCajaDTO dto) {
        dto.setUsuarioId(SecurityUtils.getUsuarioId());
        dto.setNegocioId(SecurityUtils.getNegocioId());
        return ResponseEntity.ok(sesionCajaService.abrirCaja(dto));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<CierreCajaResponseDTO> cerrarCaja(@RequestBody @Valid ArqueoCajaDTO dto) {
        return ResponseEntity.ok(sesionCajaService.cerrarCaja(dto));
    }

    @GetMapping("/actual")
    public ResponseEntity<SesionCaja> obtenerCajaActual() { // Se quitó @RequestParam
        Long usuarioId = SecurityUtils.getUsuarioId();
        return sesionCajaService.obtenerCajaActual(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
    @GetMapping
    public ResponseEntity<List<SesionCaja>> listarCajas() {
        Long negocioId = SecurityUtils.getNegocioId();
        return ResponseEntity.ok(sesionCajaService.listarSesionesPorNegocio(negocioId));
    }

    @PostMapping("/actual/movimientos")
    public ResponseEntity<?> registrarMovimiento(@RequestBody @Valid com.tinmarket.backend.dto.MovimientoCajaRequestDTO dto) {
        Long usuarioId = SecurityUtils.getUsuarioId();
        sesionCajaService.registrarMovimiento(usuarioId, dto);
        // Devolvemos el mapa acá para que el POST responda con un JSON válido y api.ts no explote
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Movimiento registrado exitosamente"));
    }

    @GetMapping("/actual/movimientos")
    public ResponseEntity<?> listarMovimientosActuales() {
        Long usuarioId = SecurityUtils.getUsuarioId();
        // Llamamos al servicio correspondiente para retornar la lista real de movimientos
        return ResponseEntity.ok(sesionCajaService.listarMovimientosCajaActual(usuarioId));
    }
}