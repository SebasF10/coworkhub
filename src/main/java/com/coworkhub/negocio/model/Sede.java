package com.coworkhub.negocio.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private LocalTime horaApertura;

    @Column(nullable = false)
    private LocalTime horaCierre;

    // Lado inverso: la clave foránea vive en Sala. LAZY (por defecto) e ignorada en el JSON.
    @OneToMany(mappedBy = "sede")
    @JsonIgnore
    private List<Sala> salas = new ArrayList<>();

    protected Sede() {
    }

    public Sede(String nombre, String ciudad, String direccion, LocalTime horaApertura, LocalTime horaCierre) {
        actualizar(nombre, ciudad, direccion, horaApertura, horaCierre);
    }

    public void actualizar(String nombre, String ciudad, String direccion, LocalTime horaApertura, LocalTime horaCierre) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCiudad() { return ciudad; }
    public String getDireccion() { return direccion; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
    public List<Sala> getSalas() { return salas; }
}