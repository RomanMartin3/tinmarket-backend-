package com.tinmarket.backend.service;


import com.tinmarket.backend.model.Categoria;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.repository.CategoriaRepository;
import com.tinmarket.backend.repository.NegocioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final NegocioRepository negocioRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, NegocioRepository negocioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.negocioRepository = negocioRepository;
    }

    public List<Categoria> listarPorNegocio(Long negocioId) {
        return categoriaRepository.findByNegocioId(negocioId);
    }

    @Transactional
    public Categoria guardar(Categoria categoria, Long negocioId) {
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        categoria.setNegocio(negocio);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoría no encontrada");
        }
        categoriaRepository.deleteById(id);
    }

}