package com.tinmarket.backend.service;

import com.tinmarket.backend.dto.ClienteRequestDTO;
import com.tinmarket.backend.dto.PagoDeudaDTO;
import com.tinmarket.backend.model.Cliente;
import com.tinmarket.backend.model.Negocio;
import com.tinmarket.backend.model.PagoCuentaCorriente;
import com.tinmarket.backend.model.SesionCaja;
import com.tinmarket.backend.model.enums.EstadoCaja;
import com.tinmarket.backend.repository.ClienteRepository;
import com.tinmarket.backend.repository.NegocioRepository;
import com.tinmarket.backend.repository.PagoCuentaCorrienteRepository;
import com.tinmarket.backend.repository.SesionCajaRepository;
import com.tinmarket.backend.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final NegocioRepository negocioRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final PagoCuentaCorrienteRepository pagoCuentaCorrienteRepository;


    public ClienteService(ClienteRepository clienteRepository,
                          NegocioRepository negocioRepository,
                          SesionCajaRepository sesionCajaRepository,
                          PagoCuentaCorrienteRepository pagoCuentaCorrienteRepository) {
        this.clienteRepository = clienteRepository;
        this.negocioRepository = negocioRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.pagoCuentaCorrienteRepository = pagoCuentaCorrienteRepository;

    }

    public List<Cliente> buscarClientes(String query) {
        // Usamos tu método getNegocioId() directamente
        Long negocioId = SecurityUtils.getNegocioId();

        if (query == null || query.trim().isEmpty()) {
            return clienteRepository.findByNegocioId(negocioId);
        }
        return clienteRepository.findByNegocioIdAndNombreContainingIgnoreCase(negocioId, query.trim());
    }

    public Cliente crearCliente(ClienteRequestDTO dto) {
        Long negocioId = SecurityUtils.getNegocioId();

        // Buscamos el negocio en la base de datos para poder asociarlo al cliente
        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado en la base de datos"));

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        // Si no mandan límite, por defecto es 50.000
        cliente.setLimiteCredito(dto.getLimiteCredito() != null ? dto.getLimiteCredito() : new BigDecimal("50000"));
        cliente.setSaldoDeuda(BigDecimal.ZERO);
        cliente.setNegocio(negocio);

        return clienteRepository.save(cliente);
    }

    public Cliente actualizarCliente(Long id, ClienteRequestDTO dto) {
        Long negocioId = SecurityUtils.getNegocioId();

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validamos que el cliente pertenezca al negocio del usuario logueado
        if (!cliente.getNegocio().getId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para editar este cliente");
        }

        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        if (dto.getLimiteCredito() != null) {
            cliente.setLimiteCredito(dto.getLimiteCredito());
        }

        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id) {
        Long negocioId = SecurityUtils.getNegocioId();

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getNegocio().getId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para eliminar este cliente");
        }

        if (cliente.getSaldoDeuda().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("No se puede eliminar un cliente con deuda pendiente en Cuenta Corriente");
        }

        clienteRepository.delete(cliente);
    }

    @Transactional
    public Cliente registrarPagoDeuda(Long id, PagoDeudaDTO dto) {
        Long negocioId = SecurityUtils.getNegocioId();

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getNegocio().getId().equals(negocioId)) {
            throw new RuntimeException("No tiene permisos para operar este cliente");
        }

        if (dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto a pagar debe ser mayor a 0");
        }

        if (dto.getMonto().compareTo(cliente.getSaldoDeuda()) > 0) {
            throw new RuntimeException("El pago no puede superar la deuda actual (" + cliente.getSaldoDeuda() + ")");
        }

        // Validación: Debe haber caja abierta
        SesionCaja cajaActual = sesionCajaRepository.findFirstByNegocioIdAndEstadoOrderByIdDesc(negocioId, EstadoCaja.ABIERTA)
                .orElseThrow(() -> new RuntimeException("Debe haber una caja abierta para registrar un cobro de deuda."));

        // 1. Descontamos el saldo del cliente
        cliente.setSaldoDeuda(cliente.getSaldoDeuda().subtract(dto.getMonto()));
        clienteRepository.save(cliente);

        // 2. Registramos la transacción histórica
        PagoCuentaCorriente pago = new PagoCuentaCorriente();
        pago.setMonto(dto.getMonto());
        pago.setTipoPago(dto.getMedioPago());
        pago.setFechaPago(LocalDateTime.now());
        pago.setCliente(cliente);
        pago.setNegocio(cliente.getNegocio());
        pago.setSesionCaja(cajaActual);

        pagoCuentaCorrienteRepository.save(pago);

        return cliente;
    }
}