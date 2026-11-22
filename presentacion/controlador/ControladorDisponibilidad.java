package com.coworkhub.presentacion.controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.service.ServicioDisponibilidad;

@RestController
@RequestMapping("/api/disponibilidad")
public class ControladorDisponibilidad {

    private final ServicioDisponibilidad servicioDisponibilidad;

    public ControladorDisponibilidad(ServicioDisponibilidad servicioDisponibilidad) {
        this.servicioDisponibilidad = servicioDisponibilidad;
    }

    // GET /api/disponibilidad?sedeId=1&fecha=2030-06-04&horaInicio=10:00&horaFin=12:00
    //     [&capacidadMinima=6][&equipamientoIds=1,2]
    @GetMapping
    public List<Sala> buscar(@RequestParam(required = false) Long sedeId,
                             @RequestParam(required = false) LocalDate fecha,
                             @RequestParam(required = false) LocalTime horaInicio,
                             @RequestParam(required = false) LocalTime horaFin,
                             @RequestParam(required = false) Integer capacidadMinima,
                             @RequestParam(required = false) Set<Long> equipamientoIds) {
        return servicioDisponibilidad.buscar(sedeId, fecha, horaInicio, horaFin, capacidadMinima, equipamientoIds);
    }
}