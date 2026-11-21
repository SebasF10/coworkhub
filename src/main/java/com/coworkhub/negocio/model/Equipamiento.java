package com.coworkhub.negocio.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
public class Equipamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // Lado inverso de la relación muchos a muchos: la tabla intermedia la declara Sala.
    @ManyToMany(mappedBy = "equipamientos")
    @JsonIgnore
    private Set<Sala> salas = new HashSet<>();

    protected Equipamiento() {
    }

    public Equipamiento(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Set<Sala> getSalas() { return salas; }
}