package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.AperturaCajaDTO;
import com.tinmarket.backend.dto.ArqueoCajaDTO;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.model.SesionCaja;
import com.tinmarket.backend.model.Usuario;
import com.tinmarket.backend.model.enums.EstadoCaja;
import com.tinmarket.backend.repository.NegocioRepository;
import com.tinmarket.backend.repository.SesionCajaRepository;
import com.tinmarket.backend.repository.UsuarioRepository;
import com.tinmarket.backend.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SesionCajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;

    public SesionCajaService(SesionCajaRepository sesionCajaRepository, NegocioRepository negocioRepository, UsuarioRepository usuarioRepository, VentaRepository ventaRepository) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional
    public SesionCaja abrirCaja(AperturaCajaDTO dto) {
        Optional<SesionCaja> cajaAbierta = sesionCajaRepository.findByUsuarioIdAndEstado(dto.getUsuarioId(), EstadoCaja.ABIERTA);
        if (cajaAbierta.isPresent()) {
            throw new RuntimeException("El usuario ya tiene una caja abierta");
        }

        Negocio negocio = negocioRepository.findById(dto.getNegocioId())
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SesionCaja nuevaSesion = new SesionCaja();
        nuevaSesion.setNegocio(negocio);
        nuevaSesion.setUsuario(usuario);
        nuevaSesion.setFechaApertura(LocalDateTime.now());
        nuevaSesion.setMontoInicial(dto.getMontoInicial());
        nuevaSesion.setEstado(EstadoCaja.ABIERTA);

        return sesionCajaRepository.save(nuevaSesion);
    }

    @Transactional
    public SesionCaja cerrarCaja(ArqueoCajaDTO dto) {
        SesionCaja sesion = sesionCajaRepository.findById(dto.getSesionId())
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        if (sesion.getEstado() == EstadoCaja.CERRADA) {
            throw new RuntimeException("Esta caja ya está cerrada");
        }

        BigDecimal totalVentas = ventaRepository.findBySesionCajaId(sesion.getId()).stream()
                .filter(v -> v.getEstado().name().equals("COMPLETADA"))
                .map(v -> v.getTotalVenta())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal esperado = sesion.getMontoInicial().add(totalVentas);
        BigDecimal diferencia = dto.getMontoFinalInformado().subtract(esperado);

        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setMontoFinalReal(dto.getMontoFinalInformado());
        sesion.setDiferencia(diferencia);
        sesion.setEstado(EstadoCaja.CERRADA);

        return sesionCajaRepository.save(sesion);
    }

    public Optional<SesionCaja> obtenerCajaActual(Long usuarioId) {
        return sesionCajaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCaja.ABIERTA);
    }
}
