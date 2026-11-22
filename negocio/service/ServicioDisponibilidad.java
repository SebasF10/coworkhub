package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.requerido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.persistence.repository.RepositorioSalas;
import com.coworkhub.persistence.repository.RepositorioSedes;

@Service
@Transactional(readOnly = true)
public class ServicioDisponibilidad {

    private final RepositorioSalas repositorioSalas;
    private final RepositorioSedes repositorioSedes;

    public ServicioDisponibilidad(RepositorioSalas repositorioSalas, RepositorioSedes repositorioSedes) {
        this.repositorioSalas = repositorioSalas;
        this.repositorioSedes = repositorioSedes;
    }

    // RF-03: salas de una sede libres en una fecha y rango horario.
    public List<Sala> buscar(Long sedeId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                             Integer capacidadMinima, Set<Long> equipamientoIds) {
        Long idSede = requerido(sedeId, "sedeId");
        requerido(fecha, "fecha");
        requerido(horaInicio, "horaInicio");
        requerido(horaFin, "horaFin");
        if (!horaInicio.isBefore(horaFin)) {
            throw new SolicitudInvalidaException("horaInicio debe ser anterior a horaFin");
        }
        int capacidad = capacidadMinima == null ? 1 : capacidadMinima;

        Sede sede = repositorioSedes.findById(idSede)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", idSede));

        // Fuera del horario de la sede no hay nada reservable (RN-03).
        if (horaInicio.isBefore(sede.getHoraApertura()) || horaFin.isAfter(sede.getHoraCierre())) {
            return List.of();
        }

        LocalDateTime inicio = fecha.atTime(horaInicio);
        LocalDateTime fin = fecha.atTime(horaFin);
        List<Sala> libres = repositorioSalas.buscarDisponibles(idSede, capacidad, inicio, fin, EstadoReserva.ACTIVOS);

        if (equipamientoIds == null || equipamientoIds.isEmpty()) {
            return libres;
        }
        return libres.stream()
                .filter(sala -> sala.getEquipamientos().stream()
                        .map(Equipamiento::getId)
                        .collect(Collectors.toSet())
                        .containsAll(equipamientoIds))
                .toList();
    }
}