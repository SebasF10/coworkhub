package com.coworkhub.negocio.service;

import static com.coworkhub.negocio.service.Validaciones.montoPositivo;
import static com.coworkhub.negocio.service.Validaciones.requerido;
import static com.coworkhub.negocio.service.Validaciones.texto;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coworkhub.presentacion.dto.SolicitudServicioAdicional;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.persistence.repository.RepositorioReservas;
import com.coworkhub.persistence.repository.RepositorioServiciosAdicionales;

@Service
@Transactional(readOnly = true)
public class ServicioServiciosAdicionales {

    private final RepositorioServiciosAdicionales repositorioServicios;
    private final RepositorioReservas repositorioReservas;

    public ServicioServiciosAdicionales(RepositorioServiciosAdicionales repositorioServicios,
                                        RepositorioReservas repositorioReservas) {
        this.repositorioServicios = repositorioServicios;
        this.repositorioReservas = repositorioReservas;
    }

    public List<ServicioAdicional> listarTodos() {
        return repositorioServicios.findAll();
    }

    public ServicioAdicional buscarPorId(Long id) {
        return repositorioServicios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", id));
    }

    @Transactional
    public ServicioAdicional crear(SolicitudServicioAdicional solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        montoPositivo(solicitud.precioUnitario(), "precioUnitario");
        if (repositorioServicios.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un servicio adicional llamado '" + nombre + "'");
        }
        return repositorioServicios.save(new ServicioAdicional(nombre, solicitud.precioUnitario()));
    }

    // Cambiar el precio no afecta a las reservas existentes: cada detalle guarda su precioUnitarioAplicado.
    @Transactional
    public ServicioAdicional actualizar(Long id, SolicitudServicioAdicional solicitud) {
        requerido(solicitud, "cuerpo de la solicitud");
        String nombre = texto(solicitud.nombre(), "nombre");
        montoPositivo(solicitud.precioUnitario(), "precioUnitario");
        ServicioAdicional servicio = buscarPorId(id);
        if (repositorioServicios.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("DUPLICADO", "Ya existe un servicio adicional llamado '" + nombre + "'");
        }
        servicio.actualizar(nombre, solicitud.precioUnitario());
        return repositorioServicios.save(servicio);
    }

    @Transactional
    public void eliminar(Long id) {
        ServicioAdicional servicio = buscarPorId(id);
        if (repositorioReservas.existsByDetallesServicioId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el servicio: está usado en reservas");
        }
        repositorioServicios.delete(servicio);
    }
}