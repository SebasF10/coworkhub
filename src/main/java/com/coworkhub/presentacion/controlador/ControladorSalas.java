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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.coworkhub.presentacion.dto.SolicitudSala;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.service.ServicioSalas;

@Tag(name = "Salas", description = "Catálogo de salas y su equipamiento")
@RestController
@RequestMapping("/api/salas")
public class ControladorSalas {

    private final ServicioSalas servicioSalas;

    public ControladorSalas(ServicioSalas servicioSalas) {
        this.servicioSalas = servicioSalas;
    }

    // GET /api/salas            → todas
    // GET /api/salas?sedeId=1   → solo las de una sede
    @GetMapping
    public List<Sala> listar(@RequestParam(required = false) Long sedeId) {
        return sedeId == null ? servicioSalas.listarTodas() : servicioSalas.listarPorSede(sedeId);
    }

    @GetMapping("/{id}")
    public Sala buscarPorId(@PathVariable Long id) {
        return servicioSalas.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sala crear(@RequestBody SolicitudSala solicitud) {
        return servicioSalas.crear(solicitud);
    }

    @PutMapping("/{id}")
    public Sala actualizar(@PathVariable Long id, @RequestBody SolicitudSala solicitud) {
        return servicioSalas.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicioSalas.eliminar(id);
    }
}