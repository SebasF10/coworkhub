package com.coworkhub.presentacion.dto;

public record SolicitudMiembro(String documento, String nombre, String email, Long planId,
                               String nombreUsuario, String contrasena) {
}