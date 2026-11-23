package com.coworkhub.security.presentacion.controlador;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coworkhub.presentacion.dto.RespuestaLogin;
import com.coworkhub.presentacion.dto.SolicitudLogin;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.security.jwt.UtilJwt;
import com.coworkhub.security.negocio.service.ServicioDetallesUsuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticación", description = "Inicio de sesión: devuelve un JWT")
@SecurityRequirements // sin candado en Swagger UI: este endpoint es público
@RestController
@RequestMapping("/auth")
public class ControladorAutenticacion {

    private final AuthenticationManager authenticationManager;
    private final ServicioDetallesUsuario servicioDetallesUsuario;
    private final UtilJwt utilJwt;

    public ControladorAutenticacion(AuthenticationManager authenticationManager,
                                    ServicioDetallesUsuario servicioDetallesUsuario, UtilJwt utilJwt) {
        this.authenticationManager = authenticationManager;
        this.servicioDetallesUsuario = servicioDetallesUsuario;
        this.utilJwt = utilJwt;
    }

    @Operation(summary = "Iniciar sesión",
            description = "Verifica las credenciales y devuelve un JWT para enviar en 'Authorization: Bearer <token>'.")
    @PostMapping("/login")
    public RespuestaLogin login(@RequestBody SolicitudLogin solicitud) {
        if (solicitud == null || solicitud.nombreUsuario() == null || solicitud.nombreUsuario().isBlank()
                || solicitud.contrasena() == null || solicitud.contrasena().isBlank()) {
            throw new SolicitudInvalidaException("nombreUsuario y contrasena son obligatorios");
        }
        // Lanza BadCredentialsException si no coinciden (se traduce a 401 en el manejador global).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(solicitud.nombreUsuario(), solicitud.contrasena()));

        UserDetails usuario = servicioDetallesUsuario.loadUserByUsername(solicitud.nombreUsuario());
        String rol = usuario.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return new RespuestaLogin(utilJwt.generarToken(usuario), rol, utilJwt.getExpiracionMinutos());
    }
}