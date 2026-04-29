package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.PromocionRequestDTO;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.model.Producto;
import com.tinmarket.backend.model.Promocion;
import com.tinmarket.backend.model.ReglaPromocion;
import com.tinmarket.backend.model.enums.TipoDescuento;
import com.tinmarket.backend.repository.NegocioRepository;
import com.tinmarket.backend.repository.ProductoRepository;
import com.tinmarket.backend.repository.PromocionRepository;
import com.tinmarket.backend.repository.ReglaPromocionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final ReglaPromocionRepository reglaPromocionRepository;
    private final NegocioRepository negocioRepository;
    private final ProductoRepository productoRepository;

    public PromocionService(PromocionRepository promocionRepository, ReglaPromocionRepository reglaPromocionRepository, NegocioRepository negocioRepository, ProductoRepository productoRepository) {
        this.promocionRepository = promocionRepository;
        this.reglaPromocionRepository = reglaPromocionRepository;
        this.negocioRepository = negocioRepository;
        this.productoRepository = productoRepository;
    }

    public List<Promocion> listarTodasPorNegocio(Long negocioId) {
        return promocionRepository.findByNegocioId(negocioId);
    }
    @Transactional
    public void alternarEstadoPromocion(Long id) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada"));
        promo.setActiva(!promo.isActiva());
        promocionRepository.save(promo);
    }

    @Transactional
    public void eliminarDefinitivamente(Long id) {
        if (!promocionRepository.existsById(id)) {
            throw new EntityNotFoundException("La promoción no existe");
        }
        promocionRepository.deleteById(id);
    }

    @Transactional
    public Promocion crearPromocion(PromocionRequestDTO dto) {
        Negocio negocio = negocioRepository.findById(dto.getNegocioId())
                .orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado"));

        Promocion promo = new Promocion();
        promo.setNegocio(negocio);
        promo.setNombre(dto.getNombre());
        promo.setFechaInicio(dto.getFechaInicio());
        promo.setFechaFin(dto.getFechaFin());
        promo.setActiva(true);

        TipoDescuento tipoDescuento = TipoDescuento.valueOf(dto.getTipoDescuento());
        promo.setTipoDescuento(tipoDescuento);
        promo.setValorDescuento(dto.getValorDescuento());
        Promocion promoGuardada = promocionRepository.save(promo);

        for (Long prodId : dto.getProductoIds()) {
            Producto producto = productoRepository.findById(prodId)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + prodId));


            if (tipoDescuento != TipoDescuento.PRECIO_FIJO_COMBO) {
                BigDecimal precioActual = producto.getPrecioVentaActual();
                BigDecimal costoActual = producto.getCostoActual();
                BigDecimal precioConDescuento;

                if (tipoDescuento == TipoDescuento.PORCENTAJE) {
                    BigDecimal descuento = precioActual.multiply(dto.getValorDescuento())
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    precioConDescuento = precioActual.subtract(descuento);
                } else {
                    precioConDescuento = precioActual.subtract(dto.getValorDescuento());
                }

                if (precioConDescuento.compareTo(costoActual) < 0) {
                    throw new IllegalArgumentException("Rentabilidad Negativa: El descuento deja a '" + producto.getNombre() +
                            "' con un precio ($" + precioConDescuento +
                            ") por debajo de su costo ($" + costoActual + ").");
                }
            }


            ReglaPromocion regla = new ReglaPromocion();
            regla.setPromocion(promoGuardada);
            regla.setProducto(producto);
            regla.setTipoDescuento(tipoDescuento);
            regla.setValorDescuento(dto.getValorDescuento()); // Si es combo, este es el precio total del combo
            regla.setCantidadNecesaria(BigDecimal.ONE);
            reglaPromocionRepository.save(regla);
        }

        return promoGuardada;
    }

    @Transactional
    public void desactivarPromocion(Long id) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada"));
        promo.setActiva(false);
        promocionRepository.save(promo);
    }
}