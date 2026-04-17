package com.tinmarket.backend.service;



import com.tinmarket.backend.dto.CodigoBarraDTO;
import com.tinmarket.backend.dto.ProductoRequestDTO;
import com.tinmarket.backend.dto.ProductoResponseDTO;
import com.tinmarket.backend.model.CodigoBarraProducto;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.model.Producto;
import com.tinmarket.backend.model.enums.UnidadMedida;
import com.tinmarket.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final NegocioRepository negocioRepository;
    private final CodigoBarraProductoRepository codigoBarraRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoService(ProductoRepository productoRepository, NegocioRepository negocioRepository, CodigoBarraProductoRepository codigoBarraRepository, CategoriaRepository categoriaRepository, ProveedorRepository proveedorRepository) {
        this.productoRepository = productoRepository;
        this.negocioRepository = negocioRepository;
        this.codigoBarraRepository = codigoBarraRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
    }

    // @Transactional asegura que si falla el guardado de un código de barra,
    // se hace rollback y no se crea el producto a medias.
    @Transactional
    public ProductoResponseDTO crearEditarProducto(ProductoRequestDTO dto) {
        // 1. Validar Negocio
        Negocio negocio = negocioRepository.findById(dto.getNegocioId())
                .orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado"));

        Producto producto = new Producto();

        // Si viene ID, es una edición
        if (dto.getId() != null) {
            producto = productoRepository.findById(dto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

            // Seguridad: Verificar que el producto pertenezca al negocio que intenta editarlo
            if (!producto.getNegocio().getId().equals(negocio.getId())) {
                throw new RuntimeException("No tiene permiso para editar este producto");
            }
        } else {
            producto.setNegocio(negocio);
            producto.setActivo(true);
        }

        // 2. Mapeo de campos simples
        producto.setNombre(dto.getNombre());
        producto.setSku(dto.getSku());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCostoActual(dto.getCostoActual());
        producto.setMargenGanancia(dto.getMargenGanancia());
        producto.setTasaIva(dto.getTasaIva() != null ? dto.getTasaIva() : BigDecimal.ZERO);

        // Lógica de Precio: Si el usuario mandó precio venta, lo usamos. Si no, lo calculamos.
        if (dto.getPrecioVentaActual() != null) {
            producto.setPrecioVentaActual(dto.getPrecioVentaActual());
        } else {
            // Precio = Costo * (1 + Margen/100)
            BigDecimal factor = BigDecimal.ONE.add(dto.getMargenGanancia().divide(new BigDecimal(100)));
            producto.setPrecioVentaActual(dto.getCostoActual().multiply(factor));
        }

        producto.setStockActual(dto.getStockActual());
        producto.setStockMinimoAlerta(dto.getStockMinimoAlerta());
        producto.setUnidadMedida(UnidadMedida.valueOf(dto.getUnidadMedida()));

        // Relaciones opcionales
        if (dto.getCategoriaId() != null) {
            producto.setCategoria(categoriaRepository.getReferenceById(dto.getCategoriaId()));
        }
        if (dto.getProveedorId() != null) {
            producto.setProveedor(proveedorRepository.getReferenceById(dto.getProveedorId()));
        }

        // 3. Guardar el Producto Padre
        Producto guardado = productoRepository.save(producto);

        // 4. Gestionar Códigos de Barra (Lo complejo)
        // Si es edición, podrías querer borrar los anteriores o actualizarlos.
        // Para simplificar MVP: Si manda lista, borramos y creamos nuevos.
        if (dto.getCodigosBarra() != null && !dto.getCodigosBarra().isEmpty()) {
            List<CodigoBarraProducto> codigosEntidades = new ArrayList<>();
            for (CodigoBarraDTO cbDto : dto.getCodigosBarra()) {
                CodigoBarraProducto cbp = new CodigoBarraProducto();
                cbp.setCodigoBarra(cbDto.getCodigo());
                cbp.setNegocio(negocio);
                cbp.setProducto(guardado); // Vinculamos al padre
                cbp.setCantidadADescontar(cbDto.getCantidadADescontar());
                codigosEntidades.add(cbp);
            }
            codigoBarraRepository.saveAll(codigosEntidades);
        }

        return mapToResponse(guardado);
    }

    public List<Producto> listarActivosPorNegocio(Long negocioId) {
        // Aquí podríamos validar si el negocio existe, si el usuario tiene permisos, etc.
        return productoRepository.findByNegocioIdAndActivoTrue(negocioId);
    }

    public List<Producto> buscarEnPOS(String query, Long negocioId) {
        return productoRepository.buscarEnPOS(query, negocioId);
    }

    // --- BAJA LÓGICA DE PRODUCTO ---
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        // Lo marcamos como inactivo en lugar de borrarlo físicamente
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoResponseDTO mapToResponse(Producto p) {
        ProductoResponseDTO resp = new ProductoResponseDTO();
        resp.setId(p.getId());
        resp.setNombre(p.getNombre());
        resp.setSku(p.getSku());
        resp.setPrecioVenta(p.getPrecioVentaActual());
        resp.setStockActual(p.getStockActual());
        resp.setUnidadMedida(p.getUnidadMedida().name());

        if (p.getCategoria() != null) resp.setCategoriaNombre(p.getCategoria().getNombre());
        if (p.getProveedor() != null) resp.setProveedorNombre(p.getProveedor().getNombre());

        // Aquí podríamos llamar al repo de códigos si quisiéramos devolverlos
        return resp;
    }
}