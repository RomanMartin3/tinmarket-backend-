package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.ReportesDTO.*;
import com.tinmarket.backend.model.Producto;
import com.tinmarket.backend.model.Venta;
import com.tinmarket.backend.repository.ProductoRepository;
import com.tinmarket.backend.repository.VentaRepository;
import com.tinmarket.backend.security.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReportesController {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public ReportesController(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/kpis")
    public ResponseEntity<KpiDTO> getKpis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ventaRepository.obtenerKPIs(SecurityUtils.getNegocioId(), desde, hasta));
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<TopProductoDTO>> getTopProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        List<TopProductoDTO> top = ventaRepository.obtenerTopProductos(SecurityUtils.getNegocioId(), desde, hasta);
        // Devolvemos solo los top 10
        return ResponseEntity.ok(top.stream().limit(10).collect(Collectors.toList()));
    }

    @GetMapping("/medios-pago")
    public ResponseEntity<List<MedioPagoDTO>> getMediosPago(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ventaRepository.obtenerVentasPorMedioPago(SecurityUtils.getNegocioId(), desde, hasta));
    }

    // Para el gráfico de evolución (Líneas/Área)
    @GetMapping("/evolucion")
    public ResponseEntity<List<VentaTiempoDTO>> getEvolucion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        List<Venta> ventas = ventaRepository.findByNegocioIdAndFechaVentaBetween(SecurityUtils.getNegocioId(), desde, hasta);

        // Si el rango es menor a 2 días, agrupamos por HORA. Si es mayor, por DÍA.
        boolean agruparPorHora = java.time.Duration.between(desde, hasta).toDays() <= 2;
        DateTimeFormatter formatter = agruparPorHora ? DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00") : DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, BigDecimal> agrupado = ventas.stream()
                .filter(v -> v.getEstado().name().equals("COMPLETADA"))
                .collect(Collectors.groupingBy(
                        v -> v.getFechaVenta().format(formatter),
                        Collectors.mapping(Venta::getTotalVenta, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<VentaTiempoDTO> resultado = agrupado.entrySet().stream()
                .map(e -> new VentaTiempoDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.getPeriodo().compareTo(b.getPeriodo()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    // Reporte en tiempo real de alertas de stock (No necesita fechas)
    @GetMapping("/alertas-stock")
    public ResponseEntity<List<Producto>> getAlertasStock() {
        List<Producto> todos = productoRepository.findByNegocioIdAndActivoTrue(SecurityUtils.getNegocioId());
        List<Producto> enAlerta = todos.stream()
                .filter(p -> p.getStockActual().compareTo(p.getStockMinimoAlerta()) <= 0)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enAlerta);
    }
}