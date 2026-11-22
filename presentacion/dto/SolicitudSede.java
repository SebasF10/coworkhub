package com.coworkhub.presentacion.dto;

import java.time.LocalTime;

public record SolicitudSede(String nombre, String ciudad, String direccion,
                            LocalTime horaApertura, LocalTime horaCierre) {
}