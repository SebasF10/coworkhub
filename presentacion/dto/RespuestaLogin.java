package com.coworkhub.presentacion.dto;

public record RespuestaLogin(String token, String rol, long expiraEnMinutos) {
}