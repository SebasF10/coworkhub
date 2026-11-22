package com.coworkhub.presentacion.controlador;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.coworkhub.negocio.service.ServicioReservas;

@RestController
@RequestMapping("/api/reservas")
public class ControladorReservas {

    private final ServicioReservas servicioReservas;

    public ControladorReservas(ServicioReservas servicioReservas) {
        this.servicioReservas = servicioReservas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@RequestBody SolicitudReserva solicitud) {
        return servicioReservas.crear(solicitud);
    }

    @GetMapping("/{id}")
    public Reserva buscarPorId(@PathVariable Long id) {
        return servicioReservas.buscarPorId(id);
    }

    // GET /api/reservas?miembroId=1
    // GET /api/reservas?salaId=1&fecha=2030-06-04
    // GET /api/reservas?estado=PENDIENTE
    @GetMapping
    public List<Reserva> listar(@RequestParam(required = false) Long miembroId,
                                @RequestParam(required = false) Long salaId,
                                @RequestParam(required = false) LocalDate fecha,
                                @RequestParam(required = false) EstadoReserva estado) {
        return servicioReservas.buscar(miembroId, salaId, fecha, estado);
    }

    @PostMapping("/{id}/servicios")
    public Reserva agregarServicio(@PathVariable Long id, @RequestBody ItemServicio item) {
        return servicioReservas.agregarServicio(id, item);
    }

    @DeleteMapping("/{id}/servicios/{servicioId}")
    public Reserva quitarServicio(@PathVariable Long id, @PathVariable Long servicioId) {
        return servicioReservas.quitarServicio(id, servicioId);
    }

    @PostMapping("/{id}/confirmar")
    public Reserva confirmar(@PathVariable Long id) {
        return servicioReservas.confirmar(id);
    }

    @PostMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id) {
        return servicioReservas.cancelar(id);
    }

    @PostMapping("/{id}/completar")
    public Reserva completar(@PathVariable Long id) {
        return servicioReservas.completar(id);
    }
}