package com.coworkhub.presentacion.excepcion;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.presentacion.dto.RespuestaError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
// Único lugar donde se traducen las excepciones a respuestas HTTP (RNF-05 y RNF-06).
// Extiende ResponseEntityExceptionHandler para cubrir también los errores propios de Spring MVC
// (JSON mal formado, parámetro faltante, ruta inexistente, método no permitido...).
@ControllerAdvice
public class ManejadorGlobalDeExcepciones extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalDeExcepciones.class);

    // ---------------------------------------------------------------- nuestras excepciones

    // Lo lanzan @PreAuthorize (RN-13) y el login: se traducen a 403 y 401 con el cuerpo estándar.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> manejarAccesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tenés permiso para realizar esta operación");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespuestaError> manejarAutenticacion(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "Usuario o contraseña incorrectos");
    }
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> manejarReglaDeNegocio(ReglaNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<RespuestaError> manejarSolicitudInvalida(SolicitudInvalidaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", ex.getMessage());
    }

    // Red de seguridad: una restricción de la base de datos (por ejemplo, un único duplicado
    // que ganó una carrera entre dos solicitudes) también es un conflicto, no un error 500.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarIntegridadDeDatos(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return responder(HttpStatus.CONFLICT, "CONFLICTO_DE_DATOS",
                "La operación viola una restricción de los datos (por ejemplo, un valor duplicado)");
    }

    // Cualquier otra excepción es un error nuestro: se registra completo y se responde algo genérico.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrió un error inesperado");
    }

    // ---------------------------------------------------------------- errores propios de Spring MVC

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return construir(status, "SOLICITUD_INVALIDA",
                "El cuerpo de la solicitud falta o no es un JSON válido (revisá tipos, fechas y valores de enumeraciones)");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return construir(status, "SOLICITUD_INVALIDA", "Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String parametro = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        return construir(status, "SOLICITUD_INVALIDA",
                "El valor '" + ex.getValue() + "' no es válido para '" + parametro + "'");
    }

    // Cualquier otro error de Spring MVC llega acá: se reemplaza su cuerpo por RespuestaError.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        String mensajeDeSpring = body instanceof ProblemDetail detalle && detalle.getDetail() != null
                ? detalle.getDetail()
                : ex.getMessage();
        return construir(statusCode, codigoPara(statusCode), mensajePara(statusCode, mensajeDeSpring));
    }

    // ---------------------------------------------------------------- utilitarios

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(RespuestaError.de(codigo, mensaje));
    }

    private ResponseEntity<Object> construir(HttpStatusCode estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(RespuestaError.de(codigo, mensaje));
    }

    // Spring redacta estos mensajes en inglés; los reemplazamos por uno propio.
    private String mensajePara(HttpStatusCode estado, String mensajePorDefecto) {
        return switch (estado.value()) {
            case 404 -> "La ruta solicitada no existe";
            case 405 -> "El método HTTP no está permitido para esta ruta";
            case 415 -> "El tipo de contenido no es soportado (usá application/json)";
            default -> mensajePorDefecto;
        };
    }

    private String codigoPara(HttpStatusCode estado) {
        return switch (estado.value()) {
            case 400 -> "SOLICITUD_INVALIDA";
            case 404 -> "NO_ENCONTRADO";
            case 405 -> "METODO_NO_PERMITIDO";
            case 415 -> "TIPO_NO_SOPORTADO";
            default -> estado.is5xxServerError() ? "ERROR_INTERNO" : "ERROR";
        };
    }
}