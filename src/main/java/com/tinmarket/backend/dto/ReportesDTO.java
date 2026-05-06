package com.tinmarket.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReportesDTO {

    @Data
    public static class KpiDTO {
        private BigDecimal totalVentas;
        private BigDecimal gananciaNeta;
        private Long cantidadVentas;
        private BigDecimal ticketPromedio;

        // El constructor recibe solo 3 parámetros desde la BD. El cuarto lo calcula Java.
        public KpiDTO(BigDecimal totalVentas, BigDecimal gananciaNeta, Long cantidadVentas) {
            this.totalVentas = totalVentas;
            this.gananciaNeta = gananciaNeta;
            this.cantidadVentas = cantidadVentas;

            if (cantidadVentas != null && cantidadVentas > 0) {
                this.ticketPromedio = totalVentas.divide(new BigDecimal(cantidadVentas), 2, RoundingMode.HALF_UP);
            } else {
                this.ticketPromedio = BigDecimal.ZERO;
            }
        }
    }

    @Data
    public static class TopProductoDTO {
        private String nombreProducto;
        private BigDecimal cantidadVendida;
        private BigDecimal totalRecaudado;

        public TopProductoDTO(String nombreProducto, BigDecimal cantidadVendida, BigDecimal totalRecaudado) {
            this.nombreProducto = nombreProducto;
            this.cantidadVendida = cantidadVendida;
            this.totalRecaudado = totalRecaudado;
        }
    }

    @Data
    public static class MedioPagoDTO {
        private String tipoPago;
        private BigDecimal montoTotal;

        public MedioPagoDTO(String tipoPago, BigDecimal montoTotal) {
            this.tipoPago = tipoPago;
            this.montoTotal = montoTotal;
        }
    }

    @Data
    public static class VentaTiempoDTO {
        private String periodo;
        private BigDecimal total;

        public VentaTiempoDTO(String periodo, BigDecimal total) {
            this.periodo = periodo;
            this.total = total;
        }
    }
}