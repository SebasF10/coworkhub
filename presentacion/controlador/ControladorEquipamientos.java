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

import com.coworkhub.presentacion.dto.SolicitudEquipamiento;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.service.ServicioEquipamientos;

@Tag(name = "Equipamientos", description = "Catálogo de equipamiento (proyector, pizarra...)")
@RestController
@RequestMapping("/api/equipamientos")
public class ControladorEquipamientos {

    private final ServicioEquipamientos servicioEquipamientos;

    public ControladorEquipamientos(ServicioEquipamientos servicioEquipamientos) {
        this.servicioEquipamientos = servicioEquipamientos;
    }

    @GetMapping
    public List<Equipamiento> listarTodos() {
        return servicioEquipamientos.listarTodos();
    }

    @GetMapping("/{id}")
    public Equipamiento buscarPorId(@PathVariable Long id) {
        return servicioEquipamientos.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipamiento crear(@RequestBody SolicitudEquipamiento solicitud) {
        return servicioEquipamientos.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Equipamiento actualizar(@PathVariable Long id, @RequestBody SolicitudEquipamiento solicitud) {
        return servicioEquipamientos.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioEquipamientos.eliminar(id);
    }
}