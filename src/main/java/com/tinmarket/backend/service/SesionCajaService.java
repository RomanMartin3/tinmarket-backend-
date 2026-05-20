package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.AperturaCajaDTO;
import com.tinmarket.backend.dto.ArqueoCajaDTO;
import com.tinmarket.backend.dto.CierreCajaResponseDTO;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.model.SesionCaja;
import com.tinmarket.backend.model.Usuario;
import com.tinmarket.backend.model.enums.EstadoCaja;
import com.tinmarket.backend.model.enums.EstadoVenta;
import com.tinmarket.backend.model.enums.TipoPago;
import com.tinmarket.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SesionCajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;

    private final PagoCuentaCorrienteRepository pagoCuentaCorrienteRepository;

    public SesionCajaService(SesionCajaRepository sesionCajaRepository,
                             NegocioRepository negocioRepository,
                             UsuarioRepository usuarioRepository,
                             VentaRepository ventaRepository,
                             PagoCuentaCorrienteRepository pagoCuentaCorrienteRepository) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
        this.pagoCuentaCorrienteRepository = pagoCuentaCorrienteRepository;
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
    public CierreCajaResponseDTO cerrarCaja(ArqueoCajaDTO dto) {
        SesionCaja sesion = sesionCajaRepository.findById(dto.getSesionId())
                .orElseThrow(() -> new RuntimeException("Sesión de caja no encontrada"));

        if (sesion.getEstado() == EstadoCaja.CERRADA) {
            throw new RuntimeException("Esta caja ya está cerrada");
        }

        // 1. Efectivo de VENTAS
        BigDecimal totalEfectivoVentas = ventaRepository.findBySesionCajaId(sesion.getId()).stream()
                .filter(v -> v.getEstado() == EstadoVenta.COMPLETADA)
                .flatMap(v -> v.getPagos().stream())
                .filter(p -> p.getTipoPago() == TipoPago.EFECTIVO)
                .map(p -> p.getMonto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Efectivo de COBROS DE CUENTA CORRIENTE
        BigDecimal totalEfectivoCobros = pagoCuentaCorrienteRepository
                .findBySesionCajaIdAndTipoPago(sesion.getId(), TipoPago.EFECTIVO)
                .stream()
                .map(p -> p.getMonto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total real que ingresó en billetes físicos
        BigDecimal totalEfectivoFisico = totalEfectivoVentas.add(totalEfectivoCobros);

        // 3. Monto Esperado en Caja
        BigDecimal esperado = sesion.getMontoInicial().add(totalEfectivoFisico);

        // 4. Diferencia
        BigDecimal diferencia = dto.getMontoFinalInformado().subtract(esperado);

        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setMontoFinalReal(dto.getMontoFinalInformado());
        sesion.setDiferencia(diferencia);
        sesion.setEstado(EstadoCaja.CERRADA);
        sesionCajaRepository.save(sesion);

        CierreCajaResponseDTO response = new CierreCajaResponseDTO();
        response.setSesionId(sesion.getId());
        response.setFechaApertura(sesion.getFechaApertura());
        response.setFechaCierre(sesion.getFechaCierre());
        response.setMontoInicial(sesion.getMontoInicial());
        response.setTotalVentasEfectivo(totalEfectivoFisico);
        response.setMontoEsperado(esperado);
        response.setMontoFinalInformado(sesion.getMontoFinalReal());
        response.setDiferencia(diferencia);
        response.setEstado(sesion.getEstado().name());

        return response;
    }

    public Optional<SesionCaja> obtenerCajaActual(Long usuarioId) {
        return sesionCajaRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCaja.ABIERTA);
    }
    public List<SesionCaja> listarSesionesPorNegocio(Long negocioId) {
        return sesionCajaRepository.findByNegocioIdOrderByFechaAperturaDesc(negocioId);
    }




}
