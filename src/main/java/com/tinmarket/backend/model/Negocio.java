package com.tinmarket.backend.model;

import com.tinmarket.backend.model.enums.CondicionFiscal;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Negocios")
public class Negocio {

@Id
@GeneratedValue( strategy = GenerationType.IDENTITY)
private Long id;

@JoinColumn(name = "negocio_id", nullable = false)
private String nombreFantasia;

private String cuit;

@Enumerated(EnumType.STRING)
@Column(name = "condicion_fiscal")
private CondicionFiscal condicionFiscal;

@Column(name = "utiliza_iva")
private Boolean utilizaIva;

@Column(name = "fecha_creacion")
private LocalDateTime fechaCreacion;

private boolean activo;

}
