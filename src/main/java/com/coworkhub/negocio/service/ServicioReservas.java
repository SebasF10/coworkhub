package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.enteroPositivo;
import static com.coworkhub.negocio.service.Validaciones.requerido;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.ItemServicio;
import com.coworkhub.presentacion.dto.SolicitudReserva;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.DetalleReserva;
import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.Reserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.persistence.repository.RepositorioMiembros;
import com.coworkhub.persistence.repository.RepositorioReservas;
import com.coworkhub.persistence.repository.RepositorioSalas;
import com.coworkhub.persistence.repository.RepositorioServiciosAdicionales;

@Service
@Transactional(readOnly = true)
public class ServicioReservas {

    private static final long DURACION_MINIMA_SEGUNDOS = 30 * 60;
    private static final long DURACION_MAXIMA_SEGUNDOS = 8 * 60 * 60;
    private static final long ANTICIPACION_MINIMA_MINUTOS = 60;
    private static final Duration ANTICIPACION_CANCELACION_SIN_CARGO = Duration.ofHours(24);
    private static final Duration ANTICIPACION_CANCELACION_MINIMA = Duration.ofHours(2);

    private final RepositorioReservas repositorioReservas;
    private final RepositorioSalas repositorioSalas;
    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioServiciosAdicionales repositorioServicios;
    private final CalculadoraCostoReserva calculadora;
    private final Clock reloj;

    public ServicioReservas(RepositorioReservas repositorioReservas, RepositorioSalas repositorioSalas,
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

    // ---------------------------------------------------------------- consultas (RF-05)

    public Reserva buscarPorId(Long id) {
        return repositorioReservas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }

    // Se filtra por miembro, o por sala + fecha, o por estado (en ese orden de prioridad).
    public List<Reserva> buscar(Long miembroId, Long salaId, LocalDate fecha, EstadoReserva estado) {
        if (miembroId != null) {
            if (!repositorioMiembros.existsById(miembroId)) {
                throw new RecursoNoEncontradoException("Miembro", miembroId);
            }
            return repositorioReservas.findByMiembroIdOrderByInicioDesc(miembroId);
        }
        if (salaId != null) {
            requerido(fecha, "fecha");
            if (!repositorioSalas.existsById(salaId)) {
                throw new RecursoNoEncontradoException("Sala", salaId);
            }
            return repositorioReservas.findBySalaIdAndInicioBetweenOrderByInicio(
                    salaId, fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX));
        }
        if (estado != null) {
            return repositorioReservas.findByEstadoOrderByInicio(estado);
        }
        throw new SolicitudInvalidaException("Indicá miembroId, o salaId junto con fecha, o estado");
    }

    // ---------------------------------------------------------------- crear (RF-04)

    // READ_COMMITTED: cada consulta ve lo ya confirmado por otras transacciones. Es imprescindible
    // con el bloqueo de la sala (ver abajo): MySQL usa por defecto REPEATABLE READ, y con ese nivel
    // esta transacción no vería la reserva que otra acaba de confirmar mientras esperaba el bloqueo.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva crear(SolicitudReserva solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        Long miembroId = requerido(solicitud.miembroId(), "miembroId");
        Long salaId = requerido(solicitud.salaId(), "salaId");
        LocalDateTime inicio = requerido(solicitud.inicio(), "inicio");
        LocalDateTime fin = requerido(solicitud.fin(), "fin");
        int asistentes = enteroPositivo(solicitud.asistentes(), "asistentes");

        Miembro miembro = repositorioMiembros.findById(miembroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", miembroId));
        // La sala se bloquea hasta terminar la transacción: así, dos solicitudes simultáneas
        // para la misma sala no pueden pasar a la vez la verificación de solapamiento.
        Sala sala = repositorioSalas.buscarParaReservar(salaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala", salaId));

        validarMiembroActivo(miembro);
        validarSala(sala, asistentes);
        long segundos = validarDuracion(inicio, fin);
        LocalDateTime ahora = LocalDateTime.now(reloj);
        validarHorario(sala.getSede(), inicio, fin, ahora);
        validarSinSolapamiento(sala, inicio, fin);
        validarLimiteDeReservas(miembro, ahora);

        Reserva reserva = new Reserva(sala, miembro, inicio, fin, asistentes);
        if (solicitud.servicios() != null) {
            for (ItemServicio item : solicitud.servicios()) {
                agregarOSumarDetalle(reserva, item);
            }
        }

        long minutos = segundos / 60;
        YearMonth mes = YearMonth.from(inicio);
        long minutosYaConsumidos = repositorioReservas.sumarMinutosConsumidos(miembroId,
                mes.atDay(1).atStartOfDay(), mes.plusMonths(1).atDay(1).atStartOfDay());
        CalculadoraCostoReserva.CostoSala costoSala =
                calculadora.calcularCostoSala(sala, miembro.getPlan(), minutos, minutosYaConsumidos);

        reserva.setMinutosConsumidos(costoSala.minutosConsumidos());
        reserva.setCostoSala(costoSala.costoSala());
        recalcularCostos(reserva);
        return repositorioReservas.save(reserva);
    }

    // ---------------------------------------------------------------- servicios adicionales (RF-06)

    @Transactional
    public Reserva agregarServicio(Long reservaId, ItemServicio item) {
        Reserva reserva = buscarPorId(reservaId);
        exigirPendienteParaServicios(reserva);
        agregarOSumarDetalle(reserva, requerido(item, "cuerpo de la solicitud"));
        recalcularCostos(reserva);
        return reserva;
    }

    @Transactional
    public Reserva quitarServicio(Long reservaId, Long servicioId) {
        Reserva reserva = buscarPorId(reservaId);
        exigirPendienteParaServicios(reserva);
        DetalleReserva detalle = reserva.getDetalles().stream()
                .filter(d -> d.getServicio().getId().equals(servicioId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio en la reserva", servicioId));
        reserva.quitarDetalle(detalle); // orphanRemoval elimina el detalle de la base de datos
        recalcularCostos(reserva);
        return reserva;
    }

    // ---------------------------------------------------------------- cambios de estado (RF-07, RF-08, RF-09)

    @Transactional
    public Reserva confirmar(Long id) {
        Reserva reserva = buscarPorId(id);
        exigirEstado(reserva, EstadoReserva.PENDIENTE, "confirmar");
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return reserva;
    }

    @Transactional
    public Reserva completar(Long id) {
        Reserva reserva = buscarPorId(id);
        exigirEstado(reserva, EstadoReserva.CONFIRMADA, "completar");
        reserva.setEstado(EstadoReserva.COMPLETADA);
        return reserva;
    }

    // RN-08: la política de cancelación depende de cuánto falta para el inicio.
    @Transactional
    public Reserva cancelar(Long id) {
        Reserva reserva = buscarPorId(id);
        if (!EstadoReserva.ACTIVOS.contains(reserva.getEstado())) {
            throw new ReglaNegocioException("RN-09",
                    "Solo se pueden cancelar reservas PENDIENTE o CONFIRMADA (estado actual: " + reserva.getEstado() + ")");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (!ahora.isBefore(reserva.getInicio())) {
            throw new ReglaNegocioException("RN-08", "La reserva ya comenzó y no se puede cancelar");
        }
        Duration anticipacion = Duration.between(ahora, reserva.getInicio());
        if (anticipacion.compareTo(ANTICIPACION_CANCELACION_MINIMA) < 0) {
            throw new ReglaNegocioException("RN-08",
                    "No se puede cancelar con menos de 2 horas de anticipación");
        }

        BigDecimal cargo = BigDecimal.ZERO;
        if (anticipacion.compareTo(ANTICIPACION_CANCELACION_SIN_CARGO) >= 0) {
            reserva.setMinutosConsumidos(0); // 24 h o más: sin cargo y se devuelven las horas del plan
        } else {
            cargo = calculadora.calcularCargoCancelacionTardia(reserva.getCostoSala());
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCargoCancelacion(cargo);
        reserva.setCostoTotal(cargo); // lo único que se cobra por una reserva cancelada es el cargo
        return reserva;
    }

    // ---------------------------------------------------------------- reglas de negocio (métodos privados)

    // RN-05
    private void validarMiembroActivo(Miembro miembro) {
        if (miembro.getEstado() != EstadoMiembro.ACTIVO) {
            throw new ReglaNegocioException("RN-05", "Solo un miembro ACTIVO puede reservar");
        }
    }

    // RN-04
    private void validarSala(Sala sala, int asistentes) {
        if (!sala.isActiva()) {
            throw new ReglaNegocioException("RN-04", "La sala '" + sala.getNombre() + "' está inactiva");
        }
        if (asistentes > sala.getCapacidad()) {
            throw new ReglaNegocioException("RN-04",
                    "Los asistentes (" + asistentes + ") superan la capacidad de la sala (" + sala.getCapacidad() + ")");
        }
    }

    // RN-02: devuelve la duración en segundos
    private long validarDuracion(LocalDateTime inicio, LocalDateTime fin) {
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ReglaNegocioException("RN-02", "La reserva debe empezar y terminar el mismo día");
        }
        long segundos = Duration.between(inicio, fin).getSeconds();
        if (segundos < DURACION_MINIMA_SEGUNDOS || segundos > DURACION_MAXIMA_SEGUNDOS
                || segundos % DURACION_MINIMA_SEGUNDOS != 0) {
            throw new ReglaNegocioException("RN-02",
                    "La duración debe ser de entre 30 minutos y 8 horas, en múltiplos de 30 minutos");
        }
        return segundos;
    }

    // RN-03
    private void validarHorario(Sede sede, LocalDateTime inicio, LocalDateTime fin, LocalDateTime ahora) {
        if (inicio.toLocalTime().isBefore(sede.getHoraApertura()) || fin.toLocalTime().isAfter(sede.getHoraCierre())) {
            throw new ReglaNegocioException("RN-03", "La reserva debe estar dentro del horario de la sede ("
                    + sede.getHoraApertura() + " a " + sede.getHoraCierre() + ")");
        }
        if (inicio.isBefore(ahora.plusMinutes(ANTICIPACION_MINIMA_MINUTOS))) {
            throw new ReglaNegocioException("RN-03", "La reserva debe hacerse con al menos 1 hora de anticipación");
        }
    }

    // RN-01
    private void validarSinSolapamiento(Sala sala, LocalDateTime inicio, LocalDateTime fin) {
        if (repositorioReservas.haySolapamiento(sala.getId(), inicio, fin, EstadoReserva.ACTIVOS)) {
            throw new ReglaNegocioException("RN-01", "La sala ya está reservada en ese horario");
        }
    }

    // RN-06
    private void validarLimiteDeReservas(Miembro miembro, LocalDateTime ahora) {
        long activas = repositorioReservas.countByMiembroIdAndEstadoInAndInicioAfter(
                miembro.getId(), EstadoReserva.ACTIVOS, ahora);
        int maximo = miembro.getPlan().getMaxReservasActivas();
        if (activas >= maximo) {
            throw new ReglaNegocioException("RN-06", "El plan " + miembro.getPlan().getNombre()
                    + " permite como máximo " + maximo + " reservas activas");
        }
    }

    // RN-10
    private void exigirPendienteParaServicios(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("RN-10",
                    "Los servicios solo se pueden modificar mientras la reserva esté PENDIENTE");
        }
    }

    // RN-09
    private void exigirEstado(Reserva reserva, EstadoReserva requerido, String accion) {
        if (reserva.getEstado() != requerido) {
            throw new ReglaNegocioException("RN-09", "No se puede " + accion + " una reserva en estado "
                    + reserva.getEstado() + " (debe estar " + requerido + ")");
        }
    }

    // Si el servicio ya está en la reserva, suma la cantidad; si no, crea un detalle nuevo
    // con el precio vigente en este momento (RN-07).
    private void agregarOSumarDetalle(Reserva reserva, ItemServicio item) {
        Long servicioId = requerido(requerido(item, "servicios").servicioId(), "servicioId");
        int cantidad = enteroPositivo(item.cantidad(), "cantidad");
        ServicioAdicional servicio = repositorioServicios.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", servicioId));
        reserva.getDetalles().stream()
                .filter(d -> d.getServicio().getId().equals(servicioId))
                .findFirst()
                .ifPresentOrElse(
                        existente -> existente.setCantidad(existente.getCantidad() + cantidad),
                        () -> reserva.agregarDetalle(new DetalleReserva(reserva, servicio, cantidad)));
    }

    // El costo de la sala no cambia al modificar servicios: solo se recalculan servicios y total.
    private void recalcularCostos(Reserva reserva) {
        reserva.setCostoServicios(calculadora.calcularCostoServicios(reserva.getDetalles()));
        reserva.setCostoTotal(reserva.getCostoSala().add(reserva.getCostoServicios()));
    }
}