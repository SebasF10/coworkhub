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

import com.coworkhub.presentacion.dto.SolicitudSede;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.negocio.service.ServicioSedes;

@Tag(name = "Sedes", description = "Catálogo de sedes")
@RestController
@RequestMapping("/api/sedes")
public class ControladorSedes {

    private final ServicioSedes servicioSedes;

    public ControladorSedes(ServicioSedes servicioSedes) {
        this.servicioSedes = servicioSedes;
    }

    @GetMapping
    public List<Sede> listarTodas() {
        return servicioSedes.listarTodas();
    }

    @GetMapping("/{id}")
    public Sede buscarPorId(@PathVariable Long id) {
        return servicioSedes.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sede crear(@RequestBody SolicitudSede solicitud) {
        return servicioSedes.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Sede actualizar(@PathVariable Long id, @RequestBody SolicitudSede solicitud) {
        return servicioSedes.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioSedes.eliminar(id);
    }
}