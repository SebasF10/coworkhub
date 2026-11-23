package com.coworkhub.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coworkhub.presentacion.dto.RespuestaError;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class ConfiguracionOpenApi {

    private static final String ESQUEMA_SEGURIDAD = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CoWorkHub API")
                        .version("1.0")
                        .description("Reservas de salas y espacios de trabajo. "
                                + "Los errores siempre responden con {error, codigo, timestamp}."))
                .components(new Components().addSecuritySchemes(ESQUEMA_SEGURIDAD,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD));
    }

    // Agrega a TODAS las operaciones las respuestas de error comunes, con el esquema RespuestaError,
    // para no repetir @ApiResponse en cada método.
    @Bean
    public OpenApiCustomizer respuestasDeErrorComunes() {
        return openApi -> {
            Map<String, Schema> esquemas = ModelConverters.getInstance().readAll(RespuestaError.class);
            esquemas.forEach((nombre, esquema) -> openApi.getComponents().addSchemas(nombre, esquema));

            openApi.getPaths().values().forEach(ruta -> ruta.readOperations().forEach(operacion -> {
                agregar(operacion, "400", "Solicitud mal formada o con datos inválidos");
                agregar(operacion, "401", "No autenticado: falta el token o no es válido");
                agregar(operacion, "403", "Sin permiso para esta operación");
                agregar(operacion, "404", "El recurso no existe");
                agregar(operacion, "409", "Se viola una regla de negocio (RN-xx) o hay un valor duplicado");
            }));
        };
    }

    private void agregar(Operation operacion, String codigo, String descripcion) {
        if (operacion.getResponses().containsKey(codigo)) {
            return;
        }
        Schema<?> referencia = new Schema<>().$ref("#/components/schemas/RespuestaError");
        operacion.getResponses().addApiResponse(codigo, new ApiResponse()
                .description(descripcion)
                .content(new Content().addMediaType("application/json", new MediaType().schema(referencia))));
    }
}