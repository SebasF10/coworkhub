package com.coworkhub.negocio.excepcion;

// Se lanza cuando un id no existe. Más adelante (Fase 4) se traduce a HTTP 404.
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No se encontró " + recurso + " con id " + id);
    }
}