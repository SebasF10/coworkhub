package com.coworkhub.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Sala;

import jakarta.persistence.LockModeType;

public interface RepositorioSalas extends JpaRepository<Sala, Long> {

    // Carga sede y equipamientos en la misma consulta (evita el problema N+1 al listar).
    @Override
    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    List<Sala> findAll();

    @Override
    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    Optional<Sala> findById(Long id);

    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    List<Sala> findBySedeIdOrderByNombre(Long sedeId);

    boolean existsBySedeId(Long sedeId);

    boolean existsBySedeIdAndNombreIgnoreCase(Long sedeId, String nombre);

    boolean existsBySedeIdAndNombreIgnoreCaseAndIdNot(Long sedeId, String nombre, Long id);

    // Bloquea la fila de la sala hasta terminar la transacción: dos reservas simultáneas
    // sobre la misma sala se procesan una después de la otra (RN-01).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.id = :id")
    Optional<Sala> buscarParaReservar(@Param("id") Long id);

    // Salas activas de una sede, con capacidad suficiente y sin reservas que se solapen.
    @EntityGraph(attributePaths = {"sede", "equipamientos"})
    @Query("""
            select s from Sala s
            where s.sede.id = :sedeId
              and s.activa = true
              and s.capacidad >= :capacidadMinima
              and not exists (
                    select r.id from Reserva r
                    where r.sala = s
                      and r.estado in :estadosActivos
                      and r.inicio < :fin
                      and r.fin > :inicio)
            order by s.nombre
            """)
    List<Sala> buscarDisponibles(@Param("sedeId") Long sedeId,
                                 @Param("capacidadMinima") int capacidadMinima,
                                 @Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin,
                                 @Param("estadosActivos") Collection<EstadoReserva> estadosActivos);
}