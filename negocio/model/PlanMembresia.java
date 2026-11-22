package com.coworkhub.negocio.model;

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
public class PlanMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private int horasIncluidasMes;

    // Porcentaje entre 0 y 100 que se descuenta a las horas excedentes.
    @Column(nullable = false)
    private int descuentoExcedente;

    @Column(nullable = false)
    private int maxReservasActivas;

    @OneToMany(mappedBy = "plan")
    @JsonIgnore
    private List<Miembro> miembros = new ArrayList<>();

    protected PlanMembresia() {
    }

    public PlanMembresia(String nombre, int horasIncluidasMes, int descuentoExcedente, int maxReservasActivas) {
        actualizar(nombre, horasIncluidasMes, descuentoExcedente, maxReservasActivas);
    }

    public void actualizar(String nombre, int horasIncluidasMes, int descuentoExcedente, int maxReservasActivas) {
        this.nombre = nombre;
        this.horasIncluidasMes = horasIncluidasMes;
        this.descuentoExcedente = descuentoExcedente;
        this.maxReservasActivas = maxReservasActivas;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public int getHorasIncluidasMes() { return horasIncluidasMes; }
    public int getDescuentoExcedente() { return descuentoExcedente; }
    public int getMaxReservasActivas() { return maxReservasActivas; }
    public List<Miembro> getMiembros() { return miembros; }
}