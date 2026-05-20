package com.tinmarket.backend.controller;

import com.tinmarket.backend.dto.ClienteRequestDTO;
import com.tinmarket.backend.dto.PagoDeudaDTO;
import com.tinmarket.backend.model.Cliente;
import com.tinmarket.backend.repository.ClienteRepository;
import com.tinmarket.backend.security.SecurityUtils;
import com.tinmarket.backend.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {


    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;

    }

    // GET /api/clientes/search?query=xxx
    @GetMapping("/search")
    public ResponseEntity<List<Cliente>> buscarClientes(@RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(clienteService.buscarClientes(query));
    }
    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.crearCliente(dto));
    }

    // PUT /api/clientes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, dto));
    }

    // DELETE /api/clientes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
    // POST /api/clientes/{id}/pagar-deuda
    @PostMapping("/{id}/pagar-deuda")
    public ResponseEntity<Cliente> registrarPagoDeuda(@PathVariable Long id, @RequestBody PagoDeudaDTO dto) {
        return ResponseEntity.ok(clienteService.registrarPagoDeuda(id, dto));
    }
}