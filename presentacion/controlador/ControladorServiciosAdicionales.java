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

import com.coworkhub.presentacion.dto.SolicitudServicioAdicional;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.negocio.service.ServicioServiciosAdicionales;

@RestController
@RequestMapping("/api/servicios-adicionales")
public class ControladorServiciosAdicionales {

    private final ServicioServiciosAdicionales servicioServicios;

    public ControladorServiciosAdicionales(ServicioServiciosAdicionales servicioServicios) {
        this.servicioServicios = servicioServicios;
    }

    @GetMapping
    public List<ServicioAdicional> listarTodos() {
        return servicioServicios.listarTodos();
    }

    @GetMapping("/{id}")
    public ServicioAdicional buscarPorId(@PathVariable Long id) {
        return servicioServicios.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioAdicional crear(@RequestBody SolicitudServicioAdicional solicitud) {
        return servicioServicios.crear(solicitud);
    }

    @PutMapping("/{id}")
    public ServicioAdicional actualizar(@PathVariable Long id, @RequestBody SolicitudServicioAdicional solicitud) {
        return servicioServicios.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioServicios.eliminar(id);
    }
}