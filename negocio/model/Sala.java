package com.coworkhub.negocio.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sede_id", "nombre"}))
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSala tipo;

    @Column(nullable = false)
    private int capacidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tarifaPorHora;

    @Column(nullable = false)
    private boolean activa = true;

    // Lado dueño (tiene la clave foránea sede_id). EAGER: toda respuesta de Sala muestra su sede.
    @ManyToOne(optional = false)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    // Lado dueño de la relación muchos a muchos (declara la tabla intermedia). LAZY por defecto.
    @ManyToMany
    @JoinTable(
            name = "sala_equipamiento",
            joinColumns = @JoinColumn(name = "sala_id"),
            inverseJoinColumns = @JoinColumn(name = "equipamiento_id")
    )
    private Set<Equipamiento> equipamientos = new HashSet<>();

    protected Sala() {
    }

    public Sala(String nombre, TipoSala tipo, int capacidad, BigDecimal tarifaPorHora, boolean activa, Sede sede) {
        actualizar(nombre, tipo, capacidad, tarifaPorHora, activa, sede);
    }

    public void actualizar(String nombre, TipoSala tipo, int capacidad, BigDecimal tarifaPorHora, boolean activa, Sede sede) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tarifaPorHora = tarifaPorHora;
        this.activa = activa;
        this.sede = sede;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoSala getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public BigDecimal getTarifaPorHora() { return tarifaPorHora; }
    public boolean isActiva() { return activa; }
    public Sede getSede() { return sede; }
    public Set<Equipamiento> getEquipamientos() { return equipamientos; }
}