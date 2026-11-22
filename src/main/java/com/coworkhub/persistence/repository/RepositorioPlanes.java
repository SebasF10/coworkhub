package com.coworkhub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.negocio.model.PlanMembresia;

public interface RepositorioPlanes extends JpaRepository<PlanMembresia, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}