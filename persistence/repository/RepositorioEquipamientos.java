package com.coworkhub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.negocio.model.Equipamiento;

public interface RepositorioEquipamientos extends JpaRepository<Equipamiento, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}