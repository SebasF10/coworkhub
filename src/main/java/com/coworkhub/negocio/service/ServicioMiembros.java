package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.requerido;
import static com.coworkhub.negocio.service.Validaciones.texto;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.SolicitudActualizarMiembro;
import com.coworkhub.presentacion.dto.SolicitudMiembro;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.persistence.repository.RepositorioMiembros;
import com.coworkhub.persistence.repository.RepositorioPlanes;
import com.coworkhub.persistence.repository.RepositorioReservas;
import com.coworkhub.security.negocio.model.Rol;
import com.coworkhub.security.negocio.model.Usuario;
import com.coworkhub.security.persistence.repository.RepositorioUsuarios;

@Service
@Transactional(readOnly = true)
public class ServicioMiembros {

    private static final int LARGO_MINIMO_CONTRASENA = 6;

    private final RepositorioMiembros repositorioMiembros;
    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioReservas repositorioReservas;
    private final PasswordEncoder passwordEncoder;

    public ServicioMiembros(RepositorioMiembros repositorioMiembros, RepositorioPlanes repositorioPlanes,
                            RepositorioUsuarios repositorioUsuarios, RepositorioReservas repositorioReservas,
                            PasswordEncoder passwordEncoder) {
        this.repositorioMiembros = repositorioMiembros;
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioReservas = repositorioReservas;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Miembro> listarTodos(EstadoMiembro estado) {
        return estado == null ? repositorioMiembros.findAll() : repositorioMiembros.findByEstadoOrderByNombre(estado);
    }

    public Miembro buscarPorId(Long id) {
        return repositorioMiembros.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Miembro", id));
    }

    // RF-02: registra al miembro junto con su usuario, en una sola transacción.
    @Transactional
    public Miembro crear(SolicitudMiembro solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String documento = texto(solicitud.documento(), "documento");
        String nombre = texto(solicitud.nombre(), "nombre");
        String email = texto(solicitud.email(), "email");
        String nombreUsuario = texto(solicitud.nombreUsuario(), "nombreUsuario");
        String contrasena = texto(solicitud.contrasena(), "contrasena");
        if (contrasena.length() < LARGO_MINIMO_CONTRASENA) {
            throw new SolicitudInvalidaException(
                    "La contrasena debe tener al menos " + LARGO_MINIMO_CONTRASENA + " caracteres");
        }
        PlanMembresia plan = buscarPlan(solicitud.planId());

        if (repositorioMiembros.existsByDocumento(documento)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el documento " + documento);
        }
        if (repositorioMiembros.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el email " + email);
        }
        if (repositorioUsuarios.existsByNombreUsuario(nombreUsuario)) {
            throw new ReglaNegocioException("RN-11", "El nombre de usuario '" + nombreUsuario + "' ya está en uso");
        }

        Miembro miembro = new Miembro(documento, nombre, email, plan);
        miembro.setUsuario(new Usuario(nombreUsuario, passwordEncoder.encode(contrasena), Rol.MIEMBRO));
        return repositorioMiembros.save(miembro);
    }

    @Transactional
    public Miembro actualizar(Long id, SolicitudActualizarMiembro solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        String email = texto(solicitud.email(), "email");
        PlanMembresia plan = buscarPlan(solicitud.planId());
        Miembro miembro = buscarPorId(id);
        if (repositorioMiembros.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un miembro con el email " + email);
        }
        miembro.actualizar(nombre, email, plan);
        return repositorioMiembros.save(miembro);
    }

    @Transactional
    public Miembro suspender(Long id) {
        Miembro miembro = buscarPorId(id);
        miembro.setEstado(EstadoMiembro.SUSPENDIDO);
        return repositorioMiembros.save(miembro);
    }

    @Transactional
    public Miembro activar(Long id) {
        Miembro miembro = buscarPorId(id);
        miembro.setEstado(EstadoMiembro.ACTIVO);
        return repositorioMiembros.save(miembro);
    }

    // RN-12: no se elimina un miembro con reservas. Si tiene usuario, cascade lo elimina también.
    @Transactional
    public void eliminar(Long id) {
        Miembro miembro = buscarPorId(id);
        if (repositorioReservas.existsByMiembroId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar el miembro: tiene reservas. Suspendelo en su lugar");
        }
        repositorioMiembros.delete(miembro);
    }

    private PlanMembresia buscarPlan(Long planId) {
        Long id = requerido(planId, "planId");
        return repositorioPlanes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }
}
