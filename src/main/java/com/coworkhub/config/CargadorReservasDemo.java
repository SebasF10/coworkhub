package com.coworkhub.config;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.negocio.model.DetalleReserva;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.Reserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.persistence.repository.RepositorioMiembros;
import com.coworkhub.persistence.repository.RepositorioReservas;
import com.coworkhub.persistence.repository.RepositorioSalas;
import com.coworkhub.persistence.repository.RepositorioServiciosAdicionales;
import com.coworkhub.negocio.service.CalculadoraCostoReserva;

// Reservas de ejemplo en distintos estados (RNF-09). Los catálogos (salas, miembros, servicios...) ya
// los cargó data.sql. Estas reservas no van en el script SQL porque sus fechas son relativas a "hoy"
// según el Clock de la aplicación, y sus costos los calcula CalculadoraCostoReserva.
// Se guardan directamente con los repositorios —sin pasar por ServicioReservas— porque algunas están en el
// pasado, y el servicio lo rechazaría (RN-03).
@Component
public class CargadorReservasDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargadorReservasDemo.class);

    private final RepositorioReservas repositorioReservas;
    private final RepositorioSalas repositorioSalas;
    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioServiciosAdicionales repositorioServicios;
    private final CalculadoraCostoReserva calculadora;
    private final Clock reloj;

    public CargadorReservasDemo(RepositorioReservas repositorioReservas, RepositorioSalas repositorioSalas,
                                RepositorioMiembros repositorioMiembros,
                                RepositorioServiciosAdicionales repositorioServicios,
                                CalculadoraCostoReserva calculadora, Clock reloj) {
        this.repositorioReservas = repositorioReservas;
        this.repositorioSalas = repositorioSalas;
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioServicios = repositorioServicios;
        this.calculadora = calculadora;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repositorioReservas.count() > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now(reloj);

        // Pasadas
        reservar("Sala Andes", "1010101", hoy.minusDays(14), "09:00", "11:00", 4, EstadoReserva.COMPLETADA, "Catering", 1);
        reservar("Sala Andes", "2020202", hoy.minusDays(7), "14:00", "15:30", 3, EstadoReserva.COMPLETADA, null, 0);
        reservar("Sala Caribe", "3030303", hoy.minusDays(5), "10:00", "11:00", 2, EstadoReserva.COMPLETADA, null, 0);
        reservar("Oficina 101", "4040404", hoy.minusDays(3), "08:00", "12:00", 2, EstadoReserva.COMPLETADA, null, 0);
        reservar("Sala Caribe", "5050505", hoy.minusDays(20), "10:00", "11:00", 2, EstadoReserva.COMPLETADA, null, 0);
        cancelar(reservar("Sala Pacífico", "1010101", hoy.minusDays(2), "09:00", "10:00", 6,
                EstadoReserva.CANCELADA, null, 0), false);

        // Futuras
        reservar("Sala Andes", "1010101", hoy.plusDays(1), "10:00", "12:00", 5, EstadoReserva.CONFIRMADA, "Catering", 2);
        reservar("Sala Andes", "2020202", hoy.plusDays(1), "14:00", "15:00", 3, EstadoReserva.PENDIENTE, null, 0);
        reservar("Sala Caribe", "3030303", hoy.plusDays(2), "09:00", "10:30", 3, EstadoReserva.PENDIENTE, null, 0);
        reservar("Oficina 101", "4040404", hoy.plusDays(2), "08:00", "10:00", 2, EstadoReserva.CONFIRMADA, null, 0);
        reservar("Sala Pacífico", "4040404", hoy.plusDays(3), "09:00", "13:00", 10, EstadoReserva.CONFIRMADA, "Soporte técnico", 1);
        reservar("Sala Pacífico", "1010101", hoy.plusDays(3), "15:00", "16:00", 6, EstadoReserva.PENDIENTE, null, 0);
        reservar("Escritorio A1", "2020202", hoy.plusDays(4), "09:00", "17:00", 1, EstadoReserva.PENDIENTE, null, 0);
        cancelar(reservar("Oficina 201", "3030303", hoy.plusDays(5), "10:00", "12:00", 3,
                EstadoReserva.CANCELADA, null, 0), true);
        reservar("Sala Andes", "4040404", hoy.plusDays(5), "09:00", "10:00", 4, EstadoReserva.PENDIENTE, null, 0);

        log.info("Reservas de ejemplo cargadas: {}", repositorioReservas.count());
    }

    private Reserva reservar(String nombreSala, String documento, LocalDate dia, String horaInicio, String horaFin,
                             int asistentes, EstadoReserva estado, String nombreServicio, int cantidad) {
        Sala sala = repositorioSalas.findAll().stream()
                .filter(s -> s.getNombre().equals(nombreSala)).findFirst().orElseThrow();
        Miembro miembro = repositorioMiembros.findAll().stream()
                .filter(m -> m.getDocumento().equals(documento)).findFirst().orElseThrow();

        Reserva reserva = new Reserva(sala, miembro, dia.atTime(LocalTime.parse(horaInicio)),
                dia.atTime(LocalTime.parse(horaFin)), asistentes);
        reserva.setEstado(estado);

        if (nombreServicio != null) {
            ServicioAdicional servicio = repositorioServicios.findAll().stream()
                    .filter(s -> s.getNombre().equals(nombreServicio)).findFirst().orElseThrow();
            reserva.agregarDetalle(new DetalleReserva(reserva, servicio, cantidad));
        }

        long minutos = Duration.between(reserva.getInicio(), reserva.getFin()).toMinutes();
        YearMonth mes = YearMonth.from(reserva.getInicio());
        long yaConsumidos = repositorioReservas.sumarMinutosConsumidos(miembro.getId(),
                mes.atDay(1).atStartOfDay(), mes.plusMonths(1).atDay(1).atStartOfDay());
        CalculadoraCostoReserva.CostoSala costoSala =
                calculadora.calcularCostoSala(sala, miembro.getPlan(), minutos, yaConsumidos);

        reserva.setMinutosConsumidos(costoSala.minutosConsumidos());
        reserva.setCostoSala(costoSala.costoSala());
        reserva.setCostoServicios(calculadora.calcularCostoServicios(reserva.getDetalles()));
        reserva.setCostoTotal(reserva.getCostoSala().add(reserva.getCostoServicios()));
        return repositorioReservas.save(reserva);
    }

    // Aplica a una reserva de ejemplo el resultado de una cancelación (RN-08).
    private void cancelar(Reserva reserva, boolean tardia) {
        if (tardia) {
            reserva.setCargoCancelacion(calculadora.calcularCargoCancelacionTardia(reserva.getCostoSala()));
        } else {
            reserva.setMinutosConsumidos(0);
        }
        reserva.setCostoTotal(reserva.getCargoCancelacion());
        repositorioReservas.save(reserva);
    }
}