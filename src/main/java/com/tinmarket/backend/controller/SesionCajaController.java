package com.tinmarket.backend.controller;


import com.tinmarket.backend.dto.AperturaCajaDTO;
import com.tinmarket.backend.dto.ArqueoCajaDTO;
import com.tinmarket.backend.model.SesionCaja;
import com.tinmarket.backend.service.SesionCajaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(sesionCajaService.abrirCaja(dto));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<SesionCaja> cerrarCaja(@RequestBody @Valid ArqueoCajaDTO dto) {
        return ResponseEntity.ok(sesionCajaService.cerrarCaja(dto));
    }

    @GetMapping("/actual")
    public ResponseEntity<SesionCaja> obtenerCajaActual(@RequestParam Long usuarioId) {
        return sesionCajaService.obtenerCajaActual(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}