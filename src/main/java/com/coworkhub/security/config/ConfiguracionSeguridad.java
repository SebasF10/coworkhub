package com.coworkhub.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.coworkhub.security.jwt.FiltroAutenticacionJwt;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en los controladores
public class ConfiguracionSeguridad {

    private static final String[] CATALOGOS = {
            "/api/sedes/**", "/api/salas/**", "/api/equipamientos/**",
            "/api/servicios-adicionales/**", "/api/planes/**"};

    private final FiltroAutenticacionJwt filtroAutenticacionJwt;
    private final PuntoEntradaJwt puntoEntradaJwt;
    private final ManejadorAccesoDenegado manejadorAccesoDenegado;

    public ConfiguracionSeguridad(FiltroAutenticacionJwt filtroAutenticacionJwt, PuntoEntradaJwt puntoEntradaJwt,
                                  ManejadorAccesoDenegado manejadorAccesoDenegado) {
        this.filtroAutenticacionJwt = filtroAutenticacionJwt;
        this.puntoEntradaJwt = puntoEntradaJwt;
        this.manejadorAccesoDenegado = manejadorAccesoDenegado;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        // Públicos: login y documentación
                        .requestMatchers("/", "/index.html", "/app.html", "/css/**", "/js/**",
                                "/auth/login", "/doc/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Catálogos: cualquier usuario autenticado los lee; solo ADMIN los modifica (RN-13)
                        .requestMatchers(HttpMethod.GET, CATALOGOS).authenticated()
                        .requestMatchers(CATALOGOS).hasRole("ADMIN")
                        // Todo lo demás requiere token; el detalle por rol y por dueño
                        // se decide con @PreAuthorize en cada método de los controladores.
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.disable())
                .formLogin(formulario -> formulario.disable())
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(puntoEntradaJwt)
                        .accessDeniedHandler(manejadorAccesoDenegado))
                .addFilterBefore(filtroAutenticacionJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
