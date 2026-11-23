package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SolicitudMiembro(
        @Schema(example = "9090909") String documento,
        @Schema(example = "Mateo Mini") String nombre,
        @Schema(example = "mateo@coworkhub.test") String email,
        @Schema(description = "Id del plan de membresía", example = "2") Long planId,
        @Schema(example = "mateo") String nombreUsuario,
        @Schema(description = "Mínimo 6 caracteres", example = "mateo123") String contrasena) {
}