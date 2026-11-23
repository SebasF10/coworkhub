package com.coworkhub.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Reserva;

public interface RepositorioReservas extends JpaRepository<Reserva, Long> {

    @Override
    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    Optional<Reserva> findById(Long id);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    List<Reserva> findByMiembroIdOrderByInicioDesc(Long miembroId);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    List<Reserva> findBySalaIdAndInicioBetweenOrderByInicio(Long salaId, LocalDateTime desde, LocalDateTime hasta);

    @EntityGraph(attributePaths = {"sala", "sala.sede", "sala.equipamientos", "miembro", "miembro.plan",
            "detalles", "detalles.servicio"})
    List<Reserva> findByEstadoOrderByInicio(EstadoReserva estado);

    boolean existsBySalaId(Long salaId);

    boolean existsBySalaIdAndEstadoIn(Long salaId, Collection<EstadoReserva> estados);

    boolean existsByMiembroId(Long miembroId);

    boolean existsByMiembroIdAndEstadoIn(Long miembroId, Collection<EstadoReserva> estados);

    // ¿La reserva pertenece a un miembro cuyo usuario tiene este nombre? (RN-13)
    boolean existsByIdAndMiembroUsuarioNombreUsuario(Long id, String nombreUsuario);

    // ¿Algún detalle de alguna reserva usa este servicio adicional?
    boolean existsByDetallesServicioId(Long servicioId);

    // Reservas activas y futuras de un miembro (RN-06).
    long countByMiembroIdAndEstadoInAndInicioAfter(Long miembroId, Collection<EstadoReserva> estados,
                                                   LocalDateTime instante);

    // Hay solapamiento si inicioNuevo < finExistente y finNuevo > inicioExistente (RN-01).
    @Query("""
            select count(r) > 0 from Reserva r
            where r.sala.id = :salaId
              and r.estado in :estadosActivos
              and r.inicio < :fin
              and r.fin > :inicio
            """)
    boolean haySolapamiento(@Param("salaId") Long salaId,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fin") LocalDateTime fin,
                            @Param("estadosActivos") Collection<EstadoReserva> estadosActivos);

    // Minutos que un miembro lleva consumidos de su plan en un rango de fechas (un mes).
    @Query("""
            select coalesce(sum(r.minutosConsumidos), 0) from Reserva r
            where r.miembro.id = :miembroId
              and r.inicio >= :desde
              and r.inicio < :hasta
            """)
    long sumarMinutosConsumidos(@Param("miembroId") Long miembroId,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta);
}