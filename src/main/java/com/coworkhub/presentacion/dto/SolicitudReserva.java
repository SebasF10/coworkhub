package com.coworkhub.presentacion.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record SolicitudReserva(
        @Schema(description = "Id del miembro que reserva", example = "1") Long miembroId,
        @Schema(description = "Id de la sala", example = "2") Long salaId,
        @Schema(description = "Inicio de la reserva", type = "string", example = "2030-06-04T16:00:00") LocalDateTime inicio,
        @Schema(description = "Fin de la reserva", type = "string", example = "2030-06-04T17:30:00") LocalDateTime fin,
        @Schema(description = "Cantidad de asistentes", example = "3") Integer asistentes,
        @Schema(description = "Servicios adicionales (opcional)") List<ItemServicio> servicios) {
}