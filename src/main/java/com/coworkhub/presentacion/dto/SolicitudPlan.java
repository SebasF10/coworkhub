package com.coworkhub.presentacion.dto;

public record SolicitudPlan(String nombre, Integer horasIncluidasMes, Integer descuentoExcedente,
                            Integer maxReservasActivas) {
}