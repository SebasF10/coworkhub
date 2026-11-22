package com.coworkhub.presentacion.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitudReserva(Long miembroId, Long salaId, LocalDateTime inicio, LocalDateTime fin,
                               Integer asistentes, List<ItemServicio> servicios) {
}