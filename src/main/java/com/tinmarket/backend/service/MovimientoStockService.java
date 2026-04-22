package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.MovimientoStockRequestDTO;
import com.tinmarket.backend.model.MovimientoStock;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.model.Producto;
import com.tinmarket.backend.model.Usuario;
import com.tinmarket.backend.model.enums.TipoMovimiento;
import com.tinmarket.backend.repository.MovimientoStockRepository;
import com.tinmarket.backend.repository.NegocioRepository;
import com.tinmarket.backend.repository.ProductoRepository;
import com.tinmarket.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoRepository productoRepository;
    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimientoStockService(MovimientoStockRepository movimientoStockRepository, ProductoRepository productoRepository, NegocioRepository negocioRepository, UsuarioRepository usuarioRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.productoRepository = productoRepository;
        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MovimientoStock> listarPorNegocio(Long negocioId) {
        // Asumiendo que tenés un findByNegocioIdOrderByFechaDesc en tu repositorio
        // Si no lo tenés, podés usar findAll() y filtrarlo, pero lo ideal es tenerlo en el Repo.
        return movimientoStockRepository.findAll().stream()
                .filter(m -> m.getNegocio().getId().equals(negocioId))
                .sorted((m1, m2) -> m2.getFecha().compareTo(m1.getFecha()))
                .toList();
    }

    @Transactional
    public MovimientoStock registrarMovimientoManual(MovimientoStockRequestDTO dto) {
        Negocio negocio = negocioRepository.findById(dto.getNegocioId())
                .orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        // Usamos tu Enum estricto
        TipoMovimiento tipo = TipoMovimiento.valueOf(dto.getTipoMovimiento());

        // Calculamos el nuevo stock basados en la intención matemática (esIngreso)
        if (dto.getEsIngreso()) {
            producto.setStockActual(producto.getStockActual().add(dto.getCantidad()));
        } else {
            producto.setStockActual(producto.getStockActual().subtract(dto.getCantidad()));
        }

        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setNegocio(negocio);
        movimiento.setUsuario(usuario);
        movimiento.setProducto(producto);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipoMovimiento(tipo);

        // Si fue egreso, guardamos la cantidad en negativo para el historial visual
        BigDecimal cantidadAuditoria = dto.getEsIngreso() ? dto.getCantidad() : dto.getCantidad().negate();
        movimiento.setCantidad(cantidadAuditoria);

        movimiento.setStockResultante(producto.getStockActual());
        movimiento.setMotivo(dto.getMotivo());

        return movimientoStockRepository.save(movimiento);
    }
}