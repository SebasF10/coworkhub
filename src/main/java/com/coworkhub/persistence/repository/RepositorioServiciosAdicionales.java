package com.coworkhub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.negocio.model.ServicioAdicional;

public interface RepositorioServiciosAdicionales extends JpaRepository<ServicioAdicional, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}