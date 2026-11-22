package com.coworkhub.negocio.service;

import java.math.BigDecimal;

import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;

// Validaciones de "forma" de los datos de entrada (campos obligatorios y rangos).
// Las reglas de negocio (RN-xx) se validan en cada servicio.
final class Validaciones {

    private Validaciones() {
    }

    static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
    }

    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    static int enteroPositivo(Integer valor, String campo) {
        if (requerido(valor, campo) < 1) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe ser mayor a cero");
        }
        return valor;
    }

    static int enteroNoNegativo(Integer valor, String campo) {
        if (requerido(valor, campo) < 0) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' no puede ser negativo");
        }
        return valor;
    }

    static int porcentaje(Integer valor, String campo) {
        if (requerido(valor, campo) < 0 || valor > 100) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe estar entre 0 y 100");
        }
        return valor;
    }

    static BigDecimal montoPositivo(BigDecimal valor, String campo) {
        if (requerido(valor, campo).signum() <= 0) {
            throw new SolicitudInvalidaException("El campo '" + campo + "' debe ser mayor a cero");
        }
        return valor;
    }
}