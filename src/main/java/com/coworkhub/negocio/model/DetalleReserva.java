package com.coworkhub.negocio.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Entidad intermedia entre Reserva y ServicioAdicional: además de la relación guarda
// datos propios (cantidad y el precio vigente al momento de reservar).
@Entity
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id")
    @JsonIgnore
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id")
    private ServicioAdicional servicio;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitarioAplicado;

    protected DetalleReserva() {
    }

    public DetalleReserva(Reserva reserva, ServicioAdicional servicio, int cantidad) {
        this.reserva = reserva;
        this.servicio = servicio;
        this.cantidad = cantidad;
        this.precioUnitarioAplicado = servicio.getPrecioUnitario();
    }

    public BigDecimal getSubtotal() {
        return precioUnitarioAplicado.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() { return id; }
    public Reserva getReserva() { return reserva; }
    public ServicioAdicional getServicio() { return servicio; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitarioAplicado() { return precioUnitarioAplicado; }
}