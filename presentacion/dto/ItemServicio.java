package com.coworkhub.presentacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ItemServicio(
        @Schema(description = "Id del servicio adicional", example = "1") Long servicioId,
        @Schema(description = "Cantidad de unidades", example = "2") Integer cantidad) {
}