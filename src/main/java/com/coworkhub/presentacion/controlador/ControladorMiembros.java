package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.presentacion.dto.ResumenConsumo;
import com.coworkhub.presentacion.dto.SolicitudActualizarMiembro;
import com.coworkhub.presentacion.dto.SolicitudMiembro;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.security.config.Permisos;
import com.coworkhub.negocio.service.ServicioConsumo;
import com.coworkhub.negocio.service.ServicioMiembros;

@Tag(name = "Miembros", description = "Registro de miembros y resumen de consumo mensual")
@RestController
@RequestMapping("/api/miembros")
public class ControladorMiembros {

    private final ServicioMiembros servicioMiembros;
    private final ServicioConsumo servicioConsumo;

    public ControladorMiembros(ServicioMiembros servicioMiembros, ServicioConsumo servicioConsumo) {
        this.servicioMiembros = servicioMiembros;
        this.servicioConsumo = servicioConsumo;
    }

    @GetMapping
    @PreAuthorize(Permisos.STAFF)
    public List<Miembro> listarTodos(@RequestParam(required = false) EstadoMiembro estado) {
        return servicioMiembros.listarTodos(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Permisos.STAFF_O_MIEMBRO_DEL_ID)
    public Miembro buscarPorId(@PathVariable Long id) {
        return servicioMiembros.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permisos.STAFF)
    public Miembro crear(@RequestBody SolicitudMiembro solicitud) {
        return servicioMiembros.crear(solicitud);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Permisos.STAFF)
    public Miembro actualizar(@PathVariable Long id, @RequestBody SolicitudActualizarMiembro solicitud) {
        return servicioMiembros.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/suspender")
    @PreAuthorize(Permisos.STAFF)
    public Miembro suspender(@PathVariable Long id) {
        return servicioMiembros.suspender(id);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize(Permisos.STAFF)
    public Miembro activar(@PathVariable Long id) {
        return servicioMiembros.activar(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Permisos.STAFF)
    public void eliminar(@PathVariable Long id) {
        servicioMiembros.eliminar(id);
    }

    // GET /api/miembros/1/consumo?mes=2030-06   (sin "mes" usa el mes actual)
    @GetMapping("/{id}/consumo")
    @PreAuthorize(Permisos.STAFF_O_MIEMBRO_DEL_ID)
    public ResumenConsumo consumo(@PathVariable Long id, @RequestParam(required = false) YearMonth mes) {
        return servicioConsumo.resumenMensual(id, mes);
    }
}
