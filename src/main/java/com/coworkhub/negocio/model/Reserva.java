package com.coworkhub.negocio.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sala_id")
    private Sala sala;

    @ManyToOne(optional = false)
    @JoinColumn(name = "miembro_id")
    private Miembro miembro;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @Column(nullable = false)
    private int asistentes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    // Minutos de la reserva que cuentan contra el consumo mensual del plan.
    // Vale 0 si la reserva se canceló con 24 h o más de anticipación.
    @Column(nullable = false)
    private int minutosConsumidos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoSala = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoServicios = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cargoCancelacion = BigDecimal.ZERO;

    // Padre de DetalleReserva: cascade guarda los detalles con la reserva y
    // orphanRemoval elimina el detalle que se quita de la lista. LAZY por defecto.
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleReserva> detalles = new ArrayList<>();

    protected Reserva() {
    }

    public Reserva(Sala sala, Miembro miembro, LocalDateTime inicio, LocalDateTime fin, int asistentes) {
        this.sala = sala;
        this.miembro = miembro;
        this.inicio = inicio;
        this.fin = fin;
        this.asistentes = asistentes;
    }

    public void agregarDetalle(DetalleReserva detalle) {
        detalles.add(detalle);
    }

    public void quitarDetalle(DetalleReserva detalle) {
        detalles.remove(detalle);
    }

    public Long getId() { return id; }
    public Sala getSala() { return sala; }
    public Miembro getMiembro() { return miembro; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public int getAsistentes() { return asistentes; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public int getMinutosConsumidos() { return minutosConsumidos; }
    public void setMinutosConsumidos(int minutosConsumidos) { this.minutosConsumidos = minutosConsumidos; }
    public BigDecimal getCostoSala() { return costoSala; }
    public void setCostoSala(BigDecimal costoSala) { this.costoSala = costoSala; }
    public BigDecimal getCostoServicios() { return costoServicios; }
    public void setCostoServicios(BigDecimal costoServicios) { this.costoServicios = costoServicios; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public BigDecimal getCargoCancelacion() { return cargoCancelacion; }
    public void setCargoCancelacion(BigDecimal cargoCancelacion) { this.cargoCancelacion = cargoCancelacion; }
    public List<DetalleReserva> getDetalles() { return detalles; }
}