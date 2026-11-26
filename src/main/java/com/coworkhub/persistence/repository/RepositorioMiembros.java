package com.coworkhub.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.EstadoMiembro;

public interface RepositorioMiembros extends JpaRepository<Miembro, Long> {

    // Trae el plan en la misma consulta (evita una consulta extra por cada miembro al listar).
    @Override
    @EntityGraph(attributePaths = "plan")
    List<Miembro> findAll();

    @EntityGraph(attributePaths = "plan")
    List<Miembro> findByEstadoOrderByNombre(EstadoMiembro estado);

    @Override
    @EntityGraph(attributePaths = "plan")
    Optional<Miembro> findById(Long id);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPlanId(Long planId);

    // Busca al miembro dueño de un usuario: recorre la relación Miembro.usuario.nombreUsuario
    Optional<Miembro> findByUsuarioNombreUsuario(String nombreUsuario);

    // ¿El miembro con este id es el dueño del usuario con este nombre? (RN-13)
    boolean existsByIdAndUsuarioNombreUsuario(Long id, String nombreUsuario);
}
