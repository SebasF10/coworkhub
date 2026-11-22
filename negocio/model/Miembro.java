package com.coworkhub.negocio.model;

import com.coworkhub.security.negocio.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String documento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMiembro estado = EstadoMiembro.ACTIVO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id")
    private PlanMembresia plan;

    // Lado dueño de la relación 1↔1 (tiene usuario_id). cascade + orphanRemoval:
    // al eliminar el miembro se elimina también su usuario. Nunca se muestra en el JSON.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    protected Miembro() {
    }

    public Miembro(String documento, String nombre, String email, PlanMembresia plan) {
        this.documento = documento;
        this.nombre = nombre;
        this.email = email;
        this.plan = plan;
    }

    public void actualizar(String nombre, String email, PlanMembresia plan) {
        this.nombre = nombre;
        this.email = email;
        this.plan = plan;
    }

    public Long getId() { return id; }
    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public EstadoMiembro getEstado() { return estado; }
    public void setEstado(EstadoMiembro estado) { this.estado = estado; }
    public PlanMembresia getPlan() { return plan; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}