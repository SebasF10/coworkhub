package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.enteroNoNegativo;
import static com.coworkhub.negocio.service.Validaciones.enteroPositivo;
import static com.coworkhub.negocio.service.Validaciones.porcentaje;
import static com.coworkhub.negocio.service.Validaciones.requerido;
import static com.coworkhub.negocio.service.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.SolicitudPlan;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.persistence.repository.RepositorioMiembros;
import com.coworkhub.persistence.repository.RepositorioPlanes;

@Service
@Transactional(readOnly = true)
public class ServicioPlanes {

    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioMiembros repositorioMiembros;

    public ServicioPlanes(RepositorioPlanes repositorioPlanes, RepositorioMiembros repositorioMiembros) {
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioMiembros = repositorioMiembros;
    }

    public List<PlanMembresia> listarTodos() {
        return repositorioPlanes.findAll();
    }

    public PlanMembresia buscarPorId(Long id) {
        return repositorioPlanes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }

    @Transactional
    public PlanMembresia crear(SolicitudPlan solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        int horas = enteroNoNegativo(solicitud.horasIncluidasMes(), "horasIncluidasMes");
        int descuento = porcentaje(solicitud.descuentoExcedente(), "descuentoExcedente");
        int maxReservas = enteroPositivo(solicitud.maxReservasActivas(), "maxReservasActivas");
        if (repositorioPlanes.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un plan llamado '" + nombre + "'");
        }
        return repositorioPlanes.save(new PlanMembresia(nombre, horas, descuento, maxReservas));
    }

    @Transactional
    public PlanMembresia actualizar(Long id, SolicitudPlan solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        int horas = enteroNoNegativo(solicitud.horasIncluidasMes(), "horasIncluidasMes");
        int descuento = porcentaje(solicitud.descuentoExcedente(), "descuentoExcedente");
        int maxReservas = enteroPositivo(solicitud.maxReservasActivas(), "maxReservasActivas");
        PlanMembresia plan = buscarPorId(id);
        if (repositorioPlanes.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un plan llamado '" + nombre + "'");
        }
        plan.actualizar(nombre, horas, descuento, maxReservas);
        return repositorioPlanes.save(plan);
    }

    @Transactional
    public void eliminar(Long id) {
        PlanMembresia plan = buscarPorId(id);
        if (repositorioMiembros.existsByPlanId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el plan: tiene miembros asociados");
        }
        repositorioPlanes.delete(plan);
    }
}