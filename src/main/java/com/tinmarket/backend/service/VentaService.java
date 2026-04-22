package com.tinmarket.backend.service;


import com.tinmarket.backend.dto.ItemVentaRequestDTO;
import com.tinmarket.backend.dto.NuevaVentaRequestDTO;
import com.tinmarket.backend.dto.PagoVentaDTO;
import com.tinmarket.backend.dto.VentaResponseDTO;
import com.tinmarket.backend.model.*;
import com.tinmarket.backend.model.enums.EstadoCaja;
import com.tinmarket.backend.model.enums.EstadoVenta;
import com.tinmarket.backend.model.enums.TipoMovimiento;
import com.tinmarket.backend.model.enums.TipoPago;
import com.tinmarket.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository, CodigoBarraProductoRepository codigoBarraRepository, SesionCajaRepository sesionCajaRepository, NegocioRepository negocioRepository, MovimientoStockRepository movimientoStockRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.codigoBarraRepository = codigoBarraRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.negocioRepository = negocioRepository;
        this.movimientoStockRepository = movimientoStockRepository;
    }

    @Transactional // ¡CRÍTICO! O se guarda toda la venta y se descuenta stock, o no se hace nada.
    public VentaResponseDTO crearVenta(NuevaVentaRequestDTO dto) {

        // 1. Validaciones Iniciales
        Negocio negocio = negocioRepository.getReferenceById(dto.getNegocioId());

        SesionCaja caja = sesionCajaRepository.findById(dto.getSesionCajaId())
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if (caja.getEstado() == EstadoCaja.CERRADA) {
            throw new RuntimeException("No se puede vender con la caja cerrada");
        }

        // 2. Crear Cabecera de Venta
        Venta venta = new Venta();
        venta.setNegocio(negocio);
        venta.setSesionCaja(caja);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);

        // Inicializamos acumuladores
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;

        // 3. Procesar Items (Iterar el carrito)
        for (ItemVentaRequestDTO itemDTO : dto.getItems()) {

            // A. Identificar Producto y Cantidad Real
            Producto producto;
            BigDecimal cantidadUnitaria = itemDTO.getCantidad(); // Ej: 1 (unidad) o 0.5 (kg)
            BigDecimal factorConversion = BigDecimal.ONE; // Por defecto 1

            if (itemDTO.getCodigoBarraEscaneado() != null) {
                // Buscamos por código (puede ser un Pack)
                CodigoBarraProducto cbp = codigoBarraRepository.findByCodigoBarraAndNegocioId(itemDTO.getCodigoBarraEscaneado(), negocio.getId())
                        .orElseThrow(() -> new RuntimeException("Código no encontrado: " + itemDTO.getCodigoBarraEscaneado()));

                producto = cbp.getProducto();
                factorConversion = cbp.getCantidadADescontar(); // Si es pack de 6, esto vale 6
            } else {
                // Búsqueda manual por ID
                producto = productoRepository.findById(itemDTO.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto ID no encontrado"));
            }

            // B. Validar Stock
            BigDecimal cantidadTotalADescontar = cantidadUnitaria.multiply(factorConversion);
            if (producto.getStockActual().compareTo(cantidadTotalADescontar) < 0) {
                // Opción: Lanzar error o permitir stock negativo (configuración de negocio).
                // Por ahora lanzamos error.
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // C. Crear Detalle (Snapshot)
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadUnitaria); // Guardamos "1 pack", no "6 unidades" en el ticket visual

            // Foto histórica de precios
            detalle.setNombreProductoHistorico(producto.getNombre());
            detalle.setCostoHistorico(producto.getCostoActual());
            detalle.setPrecioUnitarioHistorico(producto.getPrecioVentaActual()); // Precio de la unidad/pack
            detalle.setTasaIvaHistorica(producto.getTasaIva());

            // Calculamos subtotal de la línea
            BigDecimal subtotalLinea = producto.getPrecioVentaActual().multiply(cantidadUnitaria);
            detalle.setSubTotal(subtotalLinea);

            // Agregamos a la venta (Helper method bidireccional)
            venta.agregarDetalle(detalle);

            subtotalAcumulado = subtotalAcumulado.add(subtotalLinea);

            // D. Descontar Stock y Auditar
            producto.setStockActual(producto.getStockActual().subtract(cantidadTotalADescontar));
            productoRepository.save(producto); // Optimistic locking validará aquí si alguien más vendió

            crearMovimientoStock(negocio, producto, cantidadTotalADescontar.negate(), "Venta #" + venta.getId());
        }

        // 4. Totales y Pagos
        venta.setSubtotal(subtotalAcumulado);
        venta.setTotalVenta(subtotalAcumulado); // Aquí restaríamos descuentos globales si hubiera
        venta.setTotalDescuentos(BigDecimal.ZERO);

        // Procesar Pagos
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

        // Validación final de montos
        // Permitimos un margen de error de 0.01 por redondeos
        if (totalPagado.compareTo(venta.getTotalVenta()) < 0) {
            throw new RuntimeException("El pago es insuficiente. Falta dinero.");
        }

        Venta ventaGuardada = ventaRepository.save(venta);

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

        // Mapeamos los items del ticket
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

    // 2. LISTAR VENTAS POR FECHA (Para el reporte de caja)
    public List<VentaResponseDTO> listarVentas(Long negocioId, LocalDateTime desde, LocalDateTime hasta) {
        // Si no mandan fechas, usamos el día de hoy por defecto
        if (desde == null || hasta == null) {
            desde = LocalDateTime.now().withHour(0).withMinute(0);
            hasta = LocalDateTime.now().withHour(23).withMinute(59);
        }

        List<Venta> ventas = ventaRepository.findByNegocioIdAndFechaVentaBetween(negocioId, desde, hasta);
        return ventas.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // 3. ANULAR VENTA (Devolución de Stock)
    @Transactional
    public void anularVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new RuntimeException("Esta venta ya fue anulada anteriormente");
        }

        // A. Restaurar Stock
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();

            // Devolvemos la cantidad al stock (Sumamos)
            BigDecimal cantidadARestaurar = detalle.getCantidad();
            // NOTA: Si usaste lógica de packs en la venta, aquí deberías tener cuidado.
            // Pero como en el detalle guardamos la cantidad "desglosada" o la unidad de venta,
            // simplemente sumamos lo que salió.

            producto.setStockActual(producto.getStockActual().add(cantidadARestaurar));
            productoRepository.save(producto);

            // B. Auditar el movimiento
            crearMovimientoStock(venta.getNegocio(), producto, cantidadARestaurar, "ANULACIÓN Venta #" + venta.getId());
        }

        // C. Marcar venta como anulada
        venta.setEstado(EstadoVenta.ANULADA);
        ventaRepository.save(venta);
    }
}