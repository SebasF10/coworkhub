package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.enteroPositivo;
import static com.coworkhub.negocio.service.Validaciones.montoPositivo;
import static com.coworkhub.negocio.service.Validaciones.requerido;
import static com.coworkhub.negocio.service.Validaciones.texto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.SolicitudSala;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.model.EstadoReserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.persistence.repository.RepositorioEquipamientos;
import com.coworkhub.persistence.repository.RepositorioReservas;
import com.coworkhub.persistence.repository.RepositorioSalas;
import com.coworkhub.persistence.repository.RepositorioSedes;

@Service
@Transactional(readOnly = true)
public class ServicioSalas {

    private final RepositorioSalas repositorioSalas;
    private final RepositorioSedes repositorioSedes;
    private final RepositorioEquipamientos repositorioEquipamientos;
    private final RepositorioReservas repositorioReservas;

    public ServicioSalas(RepositorioSalas repositorioSalas, RepositorioSedes repositorioSedes,
                         RepositorioEquipamientos repositorioEquipamientos,
                         RepositorioReservas repositorioReservas) {
        this.repositorioSalas = repositorioSalas;
        this.repositorioSedes = repositorioSedes;
        this.repositorioEquipamientos = repositorioEquipamientos;
        this.repositorioReservas = repositorioReservas;
    }

    public List<Sala> listarTodas() {
        return repositorioSalas.findAll();
    }

    public List<Sala> listarPorSede(Long sedeId) {
        return repositorioSalas.findBySedeIdOrderByNombre(sedeId);
    }

    public Sala buscarPorId(Long id) {
        return repositorioSalas.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala", id));
    }

    @Transactional
    public Sala crear(SolicitudSala solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        Sede sede = buscarSede(solicitud.sedeId());
        if (repositorioSalas.existsBySedeIdAndNombreIgnoreCase(sede.getId(), nombre)) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala llamada '" + nombre + "' en la sede " + sede.getNombre());
        }
        Sala sala = new Sala(nombre, requerido(solicitud.tipo(), "tipo"),
                enteroPositivo(solicitud.capacidad(), "capacidad"),
                montoPositivo(solicitud.tarifaPorHora(), "tarifaPorHora"),
                solicitud.activa() == null || solicitud.activa(), sede);
        sala.getEquipamientos().addAll(buscarEquipamientos(solicitud.equipamientoIds()));
        return repositorioSalas.save(sala);
    }

    @Transactional
    public Sala actualizar(Long id, SolicitudSala solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        Sala sala = buscarPorId(id);
        Sede sede = buscarSede(solicitud.sedeId());
        if (repositorioSalas.existsBySedeIdAndNombreIgnoreCaseAndIdNot(sede.getId(), nombre, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala llamada '" + nombre + "' en la sede " + sede.getNombre());
        }
        sala.actualizar(nombre, requerido(solicitud.tipo(), "tipo"),
                enteroPositivo(solicitud.capacidad(), "capacidad"),
                montoPositivo(solicitud.tarifaPorHora(), "tarifaPorHora"),
                solicitud.activa() == null || solicitud.activa(), sede);
        sala.getEquipamientos().clear();
        sala.getEquipamientos().addAll(buscarEquipamientos(solicitud.equipamientoIds()));
        return repositorioSalas.save(sala);
    }

    @Transactional
    public void eliminar(Long id) {
        Sala sala = buscarPorId(id);
        if (repositorioReservas.existsBySalaIdAndEstadoIn(id, EstadoReserva.ACTIVOS)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sala: tiene reservas activas");
        }
        if (repositorioReservas.existsBySalaId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar la sala: tiene historial de reservas. Desactivala (activa=false) en su lugar");
        }
        repositorioSalas.delete(sala);
    }

    private Sede buscarSede(Long sedeId) {
        Long id = requerido(sedeId, "sedeId");
        return repositorioSedes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    private Set<Equipamiento> buscarEquipamientos(Set<Long> ids) {
        Set<Equipamiento> resultado = new HashSet<>();
        if (ids == null) {
            return resultado;
        }
        for (Long id : ids) {
            resultado.add(repositorioEquipamientos.findById(requerido(id, "equipamientoIds"))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id)));
        }
        return resultado;
    }
}