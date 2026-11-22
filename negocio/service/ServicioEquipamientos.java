package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.requerido;
import static com.coworkhub.negocio.service.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.SolicitudEquipamiento;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.persistence.repository.RepositorioEquipamientos;

@Service
@Transactional(readOnly = true)
public class ServicioEquipamientos {

    private final RepositorioEquipamientos repositorioEquipamientos;

    public ServicioEquipamientos(RepositorioEquipamientos repositorioEquipamientos) {
        this.repositorioEquipamientos = repositorioEquipamientos;
    }

    public List<Equipamiento> listarTodos() {
        return repositorioEquipamientos.findAll();
    }

    public Equipamiento buscarPorId(Long id) {
        return repositorioEquipamientos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id));
    }

    @Transactional
    public Equipamiento crear(SolicitudEquipamiento solicitud) {
        String nombre = texto(requerido(solicitud, "cuerpo de la solicitud").nombre(), "nombre");
        if (repositorioEquipamientos.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un equipamiento llamado '" + nombre + "'");
        }
        return repositorioEquipamientos.save(new Equipamiento(nombre));
    }

    @Transactional
    public Equipamiento actualizar(Long id, SolicitudEquipamiento solicitud) {
        String nombre = texto(requerido(solicitud, "cuerpo de la solicitud").nombre(), "nombre");
        Equipamiento equipamiento = buscarPorId(id);
        if (repositorioEquipamientos.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un equipamiento llamado '" + nombre + "'");
        }
        equipamiento.setNombre(nombre);
        return repositorioEquipamientos.save(equipamiento);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipamiento equipamiento = buscarPorId(id);
        if (!equipamiento.getSalas().isEmpty()) {
            throw new ReglaNegocioException("EN_USO", "No se puede eliminar el equipamiento: hay salas que lo usan");
        }
        repositorioEquipamientos.delete(equipamiento);
    }
}