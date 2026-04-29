package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.ItemCotizadoDTO;
import com.tinmarket.backend.model.Promocion;
import com.tinmarket.backend.model.ReglaPromocion;
import com.tinmarket.backend.model.enums.TipoDescuento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class MotorPromociones {

    public void aplicarPromociones(List<ItemCotizadoDTO> itemsCarrito, List<Promocion> promocionesActivas) {

        // --- CAMBIO 1 INICIO: Smart Sorting (Greedy por Mejor Beneficio) ---
        // En lugar de ordenar por cantidad de items, ordenamos por la plata real que le va a ahorrar al cliente.
        // Esto resuelve la optimalidad sin usar un solver pesado.
        promocionesActivas.sort((p1, p2) -> {
            BigDecimal ahorroP2 = simularAhorroPromo(p2, itemsCarrito);
            BigDecimal ahorroP1 = simularAhorroPromo(p1, itemsCarrito);
            return ahorroP2.compareTo(ahorroP1); // Descendente: la que más descuenta primero
        });
        // --- CAMBIO 1 FIN ---

        for (Promocion promo : promocionesActivas) {
            if (!promo.isActiva() || promo.getReglas() == null || promo.getReglas().isEmpty()) continue;

            int multiplicadorCombo = calcularVecesAplicables(promo, itemsCarrito);

            if (multiplicadorCombo > 0) {
                BigDecimal bdMultiplicador = new BigDecimal(multiplicadorCombo);

                // --- CAMBIO 2 INICIO: Soporte para Combos de Precio Fijo ---
                boolean esPrecioFijoCombo = promo.getReglas().get(0).getTipoDescuento() == TipoDescuento.PRECIO_FIJO_COMBO;
                BigDecimal sumaPreciosOriginales = BigDecimal.ZERO;

                if (esPrecioFijoCombo) {
                    for (ReglaPromocion regla : promo.getReglas()) {
                        sumaPreciosOriginales = sumaPreciosOriginales.add(
                                regla.getProducto().getPrecioVentaActual().multiply(regla.getCantidadNecesaria())
                        );
                    }
                }
                // --- CAMBIO 2 FIN ---

                for (ReglaPromocion regla : promo.getReglas()) {
                    ItemCotizadoDTO itemMatch = buscarItemPorProducto(itemsCarrito, regla.getProducto().getId());
                    BigDecimal cantidadAConsumir = regla.getCantidadNecesaria().multiply(bdMultiplicador);

                    // 1. Consumimos del pool
                    itemMatch.setCantidadDisponibleParaPromos(
                            itemMatch.getCantidadDisponibleParaPromos().subtract(cantidadAConsumir)
                    );

                    // 2. Calculamos el descuento
                    BigDecimal precioOriginal = itemMatch.getProducto().getPrecioVentaActual();
                    BigDecimal descuentoDeEstaRegla;

                    // --- CAMBIO 3 INICIO: Distribución proporcional ---
                    if (esPrecioFijoCombo) {
                        BigDecimal precioFijoTotalDelCombo = regla.getValorDescuento(); // Ej: $8000
                        // Peso del producto en el combo (Ej: Fernet pesa 70% del valor total original)
                        BigDecimal pesoProporcional = precioOriginal.divide(sumaPreciosOriginales, 4, RoundingMode.HALF_UP);

                        // Parte del precio fijo que le toca a este producto
                        BigDecimal nuevoPrecioProporcional = precioFijoTotalDelCombo.multiply(pesoProporcional);

                        // El descuento unitario es la diferencia entre el original y su nueva porción
                        BigDecimal descUnitario = precioOriginal.subtract(nuevoPrecioProporcional);
                        descuentoDeEstaRegla = descUnitario.multiply(cantidadAConsumir);
                    } else {
                        // Comportamiento normal para PORCENTAJE o MONTO_FIJO individual
                        descuentoDeEstaRegla = calcularDescuentoMonetarioUnitario(regla, precioOriginal).multiply(cantidadAConsumir);
                    }
                    // --- CAMBIO 3 FIN ---

                    itemMatch.setDescuentoTotal(itemMatch.getDescuentoTotal().add(descuentoDeEstaRegla));
                }
            }
        }

        // Paso Final: Calcular subtotales
        for (ItemCotizadoDTO item : itemsCarrito) {
            BigDecimal bruto = item.getProducto().getPrecioVentaActual().multiply(item.getCantidadSolicitada());
            item.setSubtotalFinal(bruto.subtract(item.getDescuentoTotal()));
        }
    }

    // --- MÉTODOS AUXILIARES NUEVOS ---

    private BigDecimal simularAhorroPromo(Promocion promo, List<ItemCotizadoDTO> itemsCarrito) {
        int vecesAplicables = calcularVecesAplicables(promo, itemsCarrito);
        if (vecesAplicables == 0) return BigDecimal.ZERO;

        boolean esPrecioFijo = promo.getReglas().get(0).getTipoDescuento() == TipoDescuento.PRECIO_FIJO_COMBO;
        BigDecimal ahorroTotalPorCombo = BigDecimal.ZERO;

        if (esPrecioFijo) {
            BigDecimal precioFijo = promo.getReglas().get(0).getValorDescuento();
            BigDecimal precioOriginalTotal = BigDecimal.ZERO;
            for(ReglaPromocion r : promo.getReglas()) {
                precioOriginalTotal = precioOriginalTotal.add(r.getProducto().getPrecioVentaActual().multiply(r.getCantidadNecesaria()));
            }
            ahorroTotalPorCombo = precioOriginalTotal.subtract(precioFijo);
        } else {
            for(ReglaPromocion r : promo.getReglas()) {
                BigDecimal descUnitario = calcularDescuentoMonetarioUnitario(r, r.getProducto().getPrecioVentaActual());
                ahorroTotalPorCombo = ahorroTotalPorCombo.add(descUnitario.multiply(r.getCantidadNecesaria()));
            }
        }

        return ahorroTotalPorCombo.multiply(new BigDecimal(vecesAplicables));
    }

    private int calcularVecesAplicables(Promocion promo, List<ItemCotizadoDTO> itemsCarrito) {
        int multiplicadorCombo = Integer.MAX_VALUE;
        for (ReglaPromocion regla : promo.getReglas()) {
            ItemCotizadoDTO itemMatch = buscarItemPorProducto(itemsCarrito, regla.getProducto().getId());
            if (itemMatch == null || itemMatch.getCantidadDisponibleParaPromos().compareTo(regla.getCantidadNecesaria()) < 0) {
                return 0; // Falla porque falta stock en el pool
            }
            int vecesPosibles = itemMatch.getCantidadDisponibleParaPromos()
                    .divide(regla.getCantidadNecesaria(), RoundingMode.DOWN).intValue();
            multiplicadorCombo = Math.min(multiplicadorCombo, vecesPosibles);
        }
        return multiplicadorCombo;
    }

    private ItemCotizadoDTO buscarItemPorProducto(List<ItemCotizadoDTO> items, Long productoId) {
        return items.stream()
                .filter(i -> i.getProducto().getId().equals(productoId))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal calcularDescuentoMonetarioUnitario(ReglaPromocion regla, BigDecimal precioOriginal) {
        if (regla.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
            return precioOriginal.multiply(regla.getValorDescuento()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (regla.getTipoDescuento() == TipoDescuento.MONTO_FIJO) {
            return regla.getValorDescuento();
        }
        return BigDecimal.ZERO;
    }
}