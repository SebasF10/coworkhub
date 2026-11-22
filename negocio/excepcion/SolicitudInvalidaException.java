package com.coworkhub.negocio.excepcion;

// Se lanza cuando faltan datos o tienen un formato inválido. Más adelante (Fase 4) se traduce a HTTP 400.
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}