package com.coworkhub.presentacion.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.presentacion.dto.ItemServicio;
import com.coworkhub.presentacion.dto.SolicitudReserva;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Reserva;
import com.coworkhub.security.config.Permisos;
import com.coworkhub.negocio.service.ServicioReservas;

@Tag(name = "Reservas", description = "Crear, consultar, modificar y cancelar reservas")
@RestController
@RequestMapping("/api/reservas")
public class ControladorReservas {

    private final ServicioReservas servicioReservas;

    public ControladorReservas(ServicioReservas servicioReservas) {
        this.servicioReservas = servicioReservas;
    }

    @Operation(summary = "Crear una reserva",
            description = "Valida RN-01 a RN-06 y calcula el costo (RN-07). Responde 201 con el costo calculado.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permisos.STAFF_O_MIEMBRO_DE_LA_SOLICITUD)
    public Reserva crear(@RequestBody SolicitudReserva solicitud) {
        return servicioReservas.crear(solicitud);
    }

    @Operation(summary = "Consultar una reserva por id")
    @GetMapping("/{id}")
    @PreAuthorize(Permisos.STAFF_O_DUENO_DE_LA_RESERVA)
    public Reserva buscarPorId(@PathVariable Long id) {
        return servicioReservas.buscarPorId(id);
    }

    // GET /api/reservas?miembroId=1
    // GET /api/reservas?salaId=1&fecha=2030-06-04
    // GET /api/reservas?estado=PENDIENTE
    @Operation(summary = "Listar reservas",
            description = "Filtrar por miembroId, o por salaId junto con fecha (yyyy-MM-dd), o por estado.")
    @GetMapping
    @PreAuthorize(Permisos.STAFF_O_FILTRO_DE_MIEMBRO)
    public List<Reserva> listar(@RequestParam(required = false) Long miembroId,
                                @RequestParam(required = false) Long salaId,
                                @RequestParam(required = false) LocalDate fecha,
                                @RequestParam(required = false) EstadoReserva estado) {
        return servicioReservas.buscar(miembroId, salaId, fecha, estado);
    }

    @Operation(summary = "Agregar un servicio adicional",
            description = "Solo con la reserva PENDIENTE (RN-10). Si el servicio ya está, suma la cantidad. Recalcula el costo.")
    @PostMapping("/{id}/servicios")
    @PreAuthorize(Permisos.STAFF_O_DUENO_DE_LA_RESERVA)
    public Reserva agregarServicio(@PathVariable Long id, @RequestBody ItemServicio item) {
        return servicioReservas.agregarServicio(id, item);
    }

    @Operation(summary = "Quitar un servicio adicional",
            description = "Solo con la reserva PENDIENTE (RN-10). Recalcula el costo.")
    @DeleteMapping("/{id}/servicios/{servicioId}")
    @PreAuthorize(Permisos.STAFF_O_DUENO_DE_LA_RESERVA)
    public Reserva quitarServicio(@PathVariable Long id, @PathVariable Long servicioId) {
        return servicioReservas.quitarServicio(id, servicioId);
    }

    @Operation(summary = "Confirmar una reserva", description = "PENDIENTE → CONFIRMADA (RN-09).")
    @PostMapping("/{id}/confirmar")
    @PreAuthorize(Permisos.STAFF)
    public Reserva confirmar(@PathVariable Long id) {
        return servicioReservas.confirmar(id);
    }

    @Operation(summary = "Cancelar una reserva",
            description = "RN-08: 24 h o más sin cargo; entre 24 h y 2 h cargo del 50 %; con menos de 2 h no se puede.")
    @PostMapping("/{id}/cancelar")
    @PreAuthorize(Permisos.STAFF_O_DUENO_DE_LA_RESERVA)
    public Reserva cancelar(@PathVariable Long id) {
        return servicioReservas.cancelar(id);
    }

    @Operation(summary = "Marcar una reserva como completada", description = "CONFIRMADA → COMPLETADA (RN-09).")
    @PostMapping("/{id}/completar")
    @PreAuthorize(Permisos.STAFF)
    public Reserva completar(@PathVariable Long id) {
        return servicioReservas.completar(id);
    }
}