package com.coworkhub.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coworkhub.negocio.model.Sede;

public interface RepositorioSedes extends JpaRepository<Sede, Long> {
}