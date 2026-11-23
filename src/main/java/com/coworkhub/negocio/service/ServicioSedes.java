package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.requerido;
import static com.coworkhub.negocio.service.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.SolicitudSede;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.persistence.repository.RepositorioSalas;
import com.coworkhub.persistence.repository.RepositorioSedes;

@Service
@Transactional(readOnly = true)
public class ServicioSedes {

    private final RepositorioSedes repositorioSedes;
    private final RepositorioSalas repositorioSalas;

    public ServicioSedes(RepositorioSedes repositorioSedes, RepositorioSalas repositorioSalas) {
        this.repositorioSedes = repositorioSedes;
        this.repositorioSalas = repositorioSalas;
    }

    public List<Sede> listarTodas() {
        return repositorioSedes.findAll();
    }

    public Sede buscarPorId(Long id) {
        return repositorioSedes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    @Transactional
    public Sede crear(SolicitudSede solicitud) {
        validar(solicitud);
        Sede sede = new Sede(texto(solicitud.nombre(), "nombre"), texto(solicitud.ciudad(), "ciudad"),
                texto(solicitud.direccion(), "direccion"), solicitud.horaApertura(), solicitud.horaCierre());
        return repositorioSedes.save(sede);
    }

    @Transactional
    public Sede actualizar(Long id, SolicitudSede solicitud) {
        validar(solicitud);
        Sede sede = buscarPorId(id);
        sede.actualizar(texto(solicitud.nombre(), "nombre"), texto(solicitud.ciudad(), "ciudad"),
                texto(solicitud.direccion(), "direccion"), solicitud.horaApertura(), solicitud.horaCierre());
        return repositorioSedes.save(sede);
    }

    @Transactional
    public void eliminar(Long id) {
        Sede sede = buscarPorId(id);
        if (repositorioSalas.existsBySedeId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sede: tiene salas asociadas");
        }
        repositorioSedes.delete(sede);
    }

    private void validar(SolicitudSede solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        requerido(solicitud.horaApertura(), "horaApertura");
        requerido(solicitud.horaCierre(), "horaCierre");
        if (!solicitud.horaApertura().isBefore(solicitud.horaCierre())) {
            throw new SolicitudInvalidaException("horaApertura debe ser anterior a horaCierre");
        }
    }
}