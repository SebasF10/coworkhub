package com.coworkhub.presentacion.dto;

import java.math.BigDecimal;
import java.util.Set;

import com.coworkhub.negocio.model.TipoSala;

public record SolicitudSala(String nombre, TipoSala tipo, Integer capacidad, BigDecimal tarifaPorHora,
                            Boolean activa, Long sedeId, Set<Long> equipamientoIds) {
}