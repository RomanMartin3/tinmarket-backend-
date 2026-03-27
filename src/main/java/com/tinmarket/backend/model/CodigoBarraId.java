package com.tinmarket.backend.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class CodigoBarraId implements Serializable {
    private String codigoBarra;
    private Long negocio;
}
