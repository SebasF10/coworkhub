package com.coworkhub.negocio.model;

import java.util.List;

public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    COMPLETADA,
    CANCELADA;

    // Estados que ocupan la sala y cuentan como "reserva activa".
    public static final List<EstadoReserva> ACTIVOS = List.of(PENDIENTE, CONFIRMADA);
}