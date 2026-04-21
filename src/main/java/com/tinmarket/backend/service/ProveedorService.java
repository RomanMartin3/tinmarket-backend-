package com.tinmarket.backend.service;

import com.tinmarket.backend.model.Proveedor;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.repository.ProveedorRepository;
import com.tinmarket.backend.repository.NegocioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final NegocioRepository negocioRepository;

    public ProveedorService(ProveedorRepository proveedorRepository, NegocioRepository negocioRepository) {
        this.proveedorRepository = proveedorRepository;
        this.negocioRepository = negocioRepository;
    }

    public List<Proveedor> listarPorNegocio(Long negocioId) {
        return proveedorRepository.findByNegocioId(negocioId);
    }

    @Transactional
    public Proveedor guardar(Proveedor proveedor, Long negocioId) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado"));
        proveedor.setNegocio(negocio);
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new EntityNotFoundException("Proveedor no encontrado");
        }
        proveedorRepository.deleteById(id);
    }
}