package com.coworkhub.security.negocio.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.coworkhub.persistence.repository.RepositorioMiembros;
import com.coworkhub.persistence.repository.RepositorioReservas;

// RN-13: decide si el usuario autenticado es dueño de un miembro o de una reserva.
// Se usa desde las expresiones de @PreAuthorize como @autorizacion.esElMiembro(...).
@Service("autorizacion")
public class ServicioAutorizacion {

    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioReservas repositorioReservas;

    public ServicioAutorizacion(RepositorioMiembros repositorioMiembros, RepositorioReservas repositorioReservas) {
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioReservas = repositorioReservas;
    }

    // ¿El usuario autenticado es el usuario del miembro con ese id?
    public boolean esElMiembro(Authentication autenticacion, Long miembroId) {
        return miembroId != null
                && repositorioMiembros.existsByIdAndUsuarioNombreUsuario(miembroId, autenticacion.getName());
    }

    // ¿La reserva pertenece a un miembro cuyo usuario es el usuario autenticado?
    public boolean esDuenoDeLaReserva(Authentication autenticacion, Long reservaId) {
        return reservaId != null
                && repositorioReservas.existsByIdAndMiembroUsuarioNombreUsuario(reservaId, autenticacion.getName());
    }
}