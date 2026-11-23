package com.coworkhub.presentacion.controlador;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.presentacion.dto.SolicitudPlan;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.negocio.service.ServicioPlanes;

@Tag(name = "Planes de membresía", description = "Catálogo de planes")
@RestController
@RequestMapping("/api/planes")
public class ControladorPlanes {

    private final ServicioPlanes servicioPlanes;

    public ControladorPlanes(ServicioPlanes servicioPlanes) {
        this.servicioPlanes = servicioPlanes;
    }

    @GetMapping
    public List<PlanMembresia> listarTodos() {
        return servicioPlanes.listarTodos();
    }

    @GetMapping("/{id}")
    public PlanMembresia buscarPorId(@PathVariable Long id) {
        return servicioPlanes.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanMembresia crear(@RequestBody SolicitudPlan solicitud) {
        return servicioPlanes.crear(solicitud);
    }

    @PutMapping("/{id}")
    public PlanMembresia actualizar(@PathVariable Long id, @RequestBody SolicitudPlan solicitud) {
        return servicioPlanes.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioPlanes.eliminar(id);
    }
}