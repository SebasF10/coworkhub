# CoWorkHub

API REST para gestionar sedes, salas, miembros, planes, servicios adicionales y reservas de un espacio de coworking.

## Requisitos y ejecución

- Java 21 o superior.
- Maven Wrapper incluido.
- H2 se usa por defecto; no requiere instalar una base de datos.

Windows:

```powershell
./mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

La aplicación queda en `http://localhost:8080`. H2 crea el esquema desde cero y carga [`src/main/resources/import.sql`](src/main/resources/import.sql) en cada arranque. Para probar:

```powershell
./mvnw.cmd test
```

## Enlaces rápidos

- [Página local de CoWorkHub](http://localhost:8080/)
- [Swagger UI](http://localhost:8080/doc/swagger-ui.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [Documento OpenAPI versionado](docs/openapi.json)
- [Colección de Insomnia](docs/insomnia-coworkhub.json)
- [Análisis del proyecto](docs/analisis.md)

## Usuarios de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `recepcion` | `recep123` | `RECEPCION` |
| `ana` | `ana123` | `MIEMBRO` |
| `luis` | `luis123` | `MIEMBRO` |

Las contraseñas se almacenan como hashes BCrypt en `import.sql`. Inicia sesión en `POST /auth/login`, copia el campo `token` y asígnalo en Insomnia a `admin_token`, `recepcion_token`, `member_token` o `member2_token`. En Swagger usa **Authorize** con `Bearer <token>`.

> Estas credenciales y la clave JWT son exclusivamente educativas; no deben reutilizarse en producción.

## Datos iniciales

[`src/main/resources/import.sql`](src/main/resources/import.sql) carga 2 sedes, 7 salas —una inactiva—, 4 equipamientos, 3 servicios, 4 planes, 5 miembros —uno suspendido—, 4 usuarios y 15 reservas en diferentes estados, incluyendo detalles de servicios.

Los nombres de tabla corresponden a las entidades JPA actuales (`sede`, `sala`, `reserva`, etc.).

## Escenarios de aceptación

La colección de [`docs/insomnia-coworkhub.json`](docs/insomnia-coworkhub.json) contiene los escenarios agrupados. Los escenarios 13A y 13B se deben enviar simultáneamente; se espera exactamente una respuesta `201` y otra `409`.

| # | Escenario | Resultado esperado |
|---:|---|---|
| 1 | Reservar una sala libre con 2 servicios | `201` con costo calculado |
| 2 | Reservar en un horario solapado | `409`, `RN-01` |
| 3 | Reservar cuando otra termina exactamente a la hora de inicio | `201` |
| 4 | Reservar 45 minutos o fuera del horario | `409` |
| 5 | Cancelar con 30 horas de anticipación | `200`, cargo `0` |
| 6 | Cancelar con 5 horas de anticipación | `200`, cargo del 50% |
| 7 | Cancelar con 1 hora de anticipación | `409` |
| 8 | Confirmar una reserva cancelada | `409` |
| 9 | Consultar la reserva de otro miembro | `403` |
| 10 | Endpoint protegido sin token o token alterado | `401` |
| 11 | Eliminar sala con reservas futuras | `409` |
| 12 | Crear sede sin nombre | `400` |
| 13 | Dos solicitudes simultáneas para la misma sala y hora | Una `201` y otra `409` |
| 14 | Listar miembros con `?estado=SUSPENDIDO` | `200`, solo suspendidos |
| 20 | Cambiar precio y consultar reserva existente | El costo existente no cambia |


## Arquitectura

- `presentacion`: controladores, DTO y manejo HTTP.
- `negocio`: entidades, servicios, reglas y excepciones.
- `persistence`: repositorios JPA.
- `security`: JWT, BCrypt y autorización por rol.
- `config`: beans, Swagger y carga de configuración.

Consulta [`docs/analisis.md`](docs/analisis.md) para actores, casos de uso, glosario, modelo de datos, trazabilidad, supuestos y fuera de alcance.
