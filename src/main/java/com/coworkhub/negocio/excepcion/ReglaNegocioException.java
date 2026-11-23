package com.coworkhub.negocio.excepcion;

// Se lanza cuando una operación viola una regla de negocio (RN-xx) o un duplicado.
// Más adelante (Fase 4) se traduce a HTTP 409.
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}