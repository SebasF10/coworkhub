package com.coworkhub.presentacion.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

// Cuerpo único de todas las respuestas de error (RNF-05).
public record RespuestaError(
        @Schema(description = "Mensaje para el usuario", example = "La sala ya está reservada en ese horario") String error,
        @Schema(description = "Código estable: RN-xx, NO_ENCONTRADO, SOLICITUD_INVALIDA...", example = "RN-01") String codigo,
        @Schema(description = "Instante del error (UTC)", example = "2030-06-03T13:00:00Z") String timestamp) {

    public static RespuestaError de(String codigo, String mensaje) {
        return new RespuestaError(mensaje, codigo, Instant.now().toString());
    }
}