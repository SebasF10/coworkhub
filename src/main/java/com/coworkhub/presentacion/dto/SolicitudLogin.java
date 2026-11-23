package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SolicitudLogin(
        @Schema(example = "ana") String nombreUsuario,
        @Schema(example = "ana123") String contrasena) {
}