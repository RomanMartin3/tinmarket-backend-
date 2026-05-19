package com.tinmarket.backend.controller;

import com.tinmarket.backend.model.Cliente;
import com.tinmarket.backend.repository.ClienteRepository;
import com.tinmarket.backend.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/search")
    public List<Cliente> buscarClientes(@RequestParam String query) {
        // CORRECCIÓN: Llamamos directo a getNegocioId() que es el método que sí existe en tu SecurityUtils
        Long negocioId = SecurityUtils.getNegocioId();
        return clienteRepository.findByNegocioIdAndNombreContainingIgnoreCase(negocioId, query);
    }
}