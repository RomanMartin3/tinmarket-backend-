package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.*;
import com.tinmarket.backend.model.*;
import com.tinmarket.backend.model.enums.*;
import com.tinmarket.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final CodigoBarraProductoRepository codigoBarraRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final NegocioRepository negocioRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    // --- CAMBIO 1 INICIO: Inyectamos PromocionRepository y el nuevo MotorPromociones ---
    private final PromocionRepository promocionRepository;
    private final MotorPromociones motorPromociones;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        CodigoBarraProductoRepository codigoBarraRepository, SesionCajaRepository sesionCajaRepository,
                        NegocioRepository negocioRepository, MovimientoStockRepository movimientoStockRepository,
                        PromocionRepository promocionRepository, MotorPromociones motorPromociones) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.codigoBarraRepository = codigoBarraRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.negocioRepository = negocioRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.promocionRepository = promocionRepository;
        this.motorPromociones = motorPromociones;
    }
    // --- CAMBIO 1 FIN ---

    @Transactional
    public VentaResponseDTO crearVenta(NuevaVentaRequestDTO dto) {

        Negocio negocio = negocioRepository.getReferenceById(dto.getNegocioId());
        SesionCaja caja = sesionCajaRepository.findById(dto.getSesionCajaId())
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if (caja.getEstado() == EstadoCaja.CERRADA) {
            throw new RuntimeException("No se puede vender con la caja cerrada");
        }

        // FASE 1 - Resolver el Carrito (Mapear a DTOs limpios)
        List<ItemCotizadoDTO> carritoCotizado = new ArrayList<>();

        for (ItemVentaRequestDTO itemDTO : dto.getItems()) {
            Producto producto;
            BigDecimal cantidadUnitaria = itemDTO.getCantidad();
            BigDecimal factorConversion = BigDecimal.ONE;

            if (itemDTO.getCodigoBarraEscaneado() != null) {
                CodigoBarraProducto cbp = codigoBarraRepository.findByCodigoBarraAndNegocioId(itemDTO.getCodigoBarraEscaneado(), negocio.getId())
                        .orElseThrow(() -> new RuntimeException("Código no encontrado: " + itemDTO.getCodigoBarraEscaneado()));
                producto = cbp.getProducto();
                factorConversion = cbp.getCantidadADescontar();
            } else {
                producto = productoRepository.findById(itemDTO.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto ID no encontrado"));
            }

            BigDecimal cantidadFisicaTotal = cantidadUnitaria.multiply(factorConversion);
            if (producto.getStockActual().compareTo(cantidadFisicaTotal) < 0) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            carritoCotizado.add(new ItemCotizadoDTO(producto, cantidadUnitaria, cantidadFisicaTotal));
        }

        // --- CAMBIO 1 INICIO: FASE 2 Optimizada - Evitar colapso de DB ---
        // 1. Extraemos los IDs que realmente están en este carrito
        java.util.Set<Long> productosEnCarrito = carritoCotizado.stream()
                .map(i -> i.getProducto().getId())
                .collect(Collectors.toSet());

        // 2. Traemos SOLO las promociones activas que involucran a estos productos (Query salvavidas)
        List<Promocion> promosActivas = promocionRepository.findActivasPorNegocioYProductos(negocio.getId(), productosEnCarrito);

        // 3. Delegamos al Motor (que ahora ordena por ahorro real)
        motorPromociones.aplicarPromociones(carritoCotizado, promosActivas);
        // --- CAMBIO 1 FIN ---


        // FASE 3 - Persistencia (Igual a la versión anterior)
        Venta venta = new Venta();
        venta.setNegocio(negocio);
        venta.setSesionCaja(caja);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);

        BigDecimal subtotalBruto = BigDecimal.ZERO;
        BigDecimal totalDescuentosGrales = BigDecimal.ZERO;

        for (ItemCotizadoDTO item : carritoCotizado) {
            BigDecimal precioOriginal = item.getProducto().getPrecioVentaActual();
            subtotalBruto = subtotalBruto.add(precioOriginal.multiply(item.getCantidadSolicitada()));
            totalDescuentosGrales = totalDescuentosGrales.add(item.getDescuentoTotal());

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidadSolicitada());
            detalle.setCantidadFisica(item.getCantidadFisicaTotal());

            detalle.setNombreProductoHistorico(item.getProducto().getNombre());
            detalle.setCostoHistorico(item.getProducto().getCostoActual());
            BigDecimal precioUnitarioConDesc = item.getSubtotalFinal().divide(item.getCantidadSolicitada(), 2, RoundingMode.HALF_UP);
            detalle.setPrecioUnitarioHistorico(precioUnitarioConDesc);
            detalle.setTasaIvaHistorica(item.getProducto().getTasaIva());
            detalle.setSubTotal(item.getSubtotalFinal());

            venta.agregarDetalle(detalle);

            item.getProducto().setStockActual(item.getProducto().getStockActual().subtract(item.getCantidadFisicaTotal()));
            productoRepository.save(item.getProducto());
        }

        venta.setSubtotal(subtotalBruto);
        venta.setTotalDescuentos(totalDescuentosGrales);
        venta.setTotalVenta(subtotalBruto.subtract(totalDescuentosGrales));

        BigDecimal totalPagado = BigDecimal.ZERO;
        for (PagoVentaDTO pagoDTO : dto.getPagos()) {
            PagoVenta pago = new PagoVenta();
            pago.setVenta(venta);
            pago.setTipoPago(TipoPago.valueOf(pagoDTO.getTipoPago()));
            pago.setMonto(pagoDTO.getMonto());
            pago.setReferenciaPago(pagoDTO.getReferencia());

            venta.getPagos().add(pago);
            totalPagado = totalPagado.add(pagoDTO.getMonto());
        }

        if (totalPagado.compareTo(venta.getTotalVenta()) < 0) {
            throw new RuntimeException("Pago insuficiente. Faltan $" + venta.getTotalVenta().subtract(totalPagado));
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        for(ItemCotizadoDTO item : carritoCotizado) {
            crearMovimientoStock(negocio, item.getProducto(), item.getCantidadFisicaTotal().negate(), "Venta #" + ventaGuardada.getId());
        }

        return mapToResponse(ventaGuardada);
    }

    private void crearMovimientoStock(Negocio negocio, Producto producto, BigDecimal cantidad, String motivo) {
        MovimientoStock mov = new MovimientoStock();
        mov.setNegocio(negocio);
        mov.setProducto(producto);
        mov.setFecha(LocalDateTime.now());
        mov.setTipoMovimiento(TipoMovimiento.VENTA);
        mov.setCantidad(cantidad);
        mov.setStockResultante(producto.getStockActual());
        mov.setMotivo(motivo);
        movimientoStockRepository.save(mov);
    }

    private VentaResponseDTO mapToResponse(Venta v) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(v.getId());
        dto.setFecha(v.getFechaVenta());
        dto.setTotal(v.getTotalVenta());
        dto.setEstado(v.getEstado().name());

        List<com.tinmarket.backend.dto.DetalleTicketDTO> detalles = v.getDetalles().stream().map(d -> {
            com.tinmarket.backend.dto.DetalleTicketDTO item = new com.tinmarket.backend.dto.DetalleTicketDTO();
            item.setNombreProducto(d.getNombreProductoHistorico());
            item.setCantidad(d.getCantidad());
            item.setPrecioUnitario(d.getPrecioUnitarioHistorico());
            item.setSubTotal(d.getSubTotal());
            return item;
        }).collect(Collectors.toList());

        dto.setItems(detalles);
        return dto;
    }
    public VentaResponseDTO buscarPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        return mapToResponse(venta);
    }

    public List<VentaResponseDTO> listarVentas(Long negocioId, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            desde = LocalDateTime.now().withHour(0).withMinute(0);
            hasta = LocalDateTime.now().withHour(23).withMinute(59);
        }
        List<Venta> ventas = ventaRepository.findByNegocioIdAndFechaVentaBetween(negocioId, desde, hasta);
        return ventas.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void anularVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new RuntimeException("Esta venta ya fue anulada anteriormente");
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();

            // --- CAMBIO 5 INICIO: Solución al Bug de Anulación de Stock ---
            // Ahora devolvemos la cantidad FISICA que se descontó (incluye factor conversión de códigos de barra)
            BigDecimal cantidadARestaurar = detalle.getCantidadFisica();
            // --- CAMBIO 5 FIN ---

            producto.setStockActual(producto.getStockActual().add(cantidadARestaurar));
            productoRepository.save(producto);

            crearMovimientoStock(venta.getNegocio(), producto, cantidadARestaurar, "ANULACIÓN Venta #" + venta.getId());
        }

        venta.setEstado(EstadoVenta.ANULADA);
        ventaRepository.save(venta);
    }
}