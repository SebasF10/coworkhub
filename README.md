# CoWorkHub

API REST y aplicación web para administrar un espacio de coworking: sedes, salas, miembros, planes, servicios adicionales y reservas. Incluye control de disponibilidad, cálculo de consumo y costos, autenticación JWT y permisos por rol.

<p align="center">
  <strong>Java 21 · Spring Boot 4.2 · Spring Data JPA · Spring Security · JWT · Maven</strong><br>
  <a href="#inicio-rápido">Inicio rápido</a> · <a href="#postman">Postman</a> · <a href="#api-y-rutas">API</a> · <a href="#seguridad-y-datos">Seguridad</a>
</p>

> **Proyecto educativo:** el repositorio contiene usuarios, contraseñas de demostración, una clave JWT de desarrollo y registros sintéticos para facilitar las pruebas. No reutilices estos valores en producción. Consulta [Seguridad y datos](#seguridad-y-datos).

## Accesos rápidos

- [Colección Postman: 14 escenarios importables](postman/CoWorkHub.postman_collection.json)
- [Aplicación local](http://localhost:8080/) · [Swagger UI](http://localhost:8080/doc/swagger-ui.html) · [OpenAPI](http://localhost:8080/v3/api-docs)
- [Análisis del proyecto](docs/analisis.md) · [OpenAPI versionado](docs/openapi.json)

## Contenido

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Inicio rápido](#inicio-rápido)
- [Base de datos con Docker](#base-de-datos-con-docker)
- [Configuración](#configuración)
- [Acceso a la aplicación](#acceso-a-la-aplicación)
- [Usuarios de demostración](#usuarios-de-demostración)
- [Roles](#roles)
- [API y rutas](#api-y-rutas)
- [Postman y pruebas](#postman)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Seguridad y datos](#seguridad-y-datos)
- [Documentación adicional](#documentación-adicional)

## Características

- Administración de sedes, salas, equipamientos, servicios adicionales, planes y miembros.
- Consulta de disponibilidad por sede, fecha, horario, capacidad y equipamientos.
- Reservas con control de solapamientos y concurrencia.
- Cálculo de horas incluidas, excedentes, costos y cargos por cancelación.
- Autenticación JWT y autorización por rol y propietario.
- Interfaz web servida por Spring Boot.

## Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Backend | Spring Boot 4.2, Spring MVC, Spring Data JPA |
| Seguridad | Spring Security, JWT (jjwt), BCrypt |
| Bases de datos | H2 (predeterminada), MySQL 8.4, PostgreSQL 16 |
| API | REST, OpenAPI y Swagger UI |
| Frontend | HTML, CSS y JavaScript |
| Build | Maven Wrapper |
| Contenedores | Docker Compose opcional |

## Inicio rápido

### Requisitos

- JDK 21 o superior.
- Git.
- Docker Desktop solo para MySQL o PostgreSQL.
- Maven no es necesario: el wrapper está incluido.

### Clonar y ejecutar con H2

~~~bash
git clone <URL_DEL_REPOSITORIO>
cd coworkhub
~~~

En Windows PowerShell:

~~~powershell
.\mvnw.cmd spring-boot:run
~~~

En Linux o macOS:

~~~bash
chmod +x mvnw
./mvnw spring-boot:run
~~~

La aplicación inicia en http://localhost:8080. H2 corre en memoria y no requiere instalación. Sus datos de prueba se recargan al reiniciar. Para detenerla, usa Ctrl+C en la terminal.

> El Maven Wrapper puede descargar dependencias durante el primer arranque; se necesita conexión a Internet para esa descarga.

## Base de datos con Docker

H2 es la opción predeterminada. Para MySQL o PostgreSQL, levanta el servicio elegido y arranca Spring con el perfil correspondiente.

### MySQL 8.4

~~~powershell
docker compose --profile mysql up -d
docker compose --profile mysql ps
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
~~~

El contenedor publica el puerto 3306. Si está ocupado, configura MYSQL_PORT=3307 antes de levantar Docker y COWORKHUB_DB_PORT=3307 antes de arrancar Spring. Valores locales: base coworkhub, usuario coworkhub, contraseña coworkhub y contraseña root root.

### PostgreSQL 16

~~~powershell
docker compose --profile postgres up -d
docker compose --profile postgres ps
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
~~~

El puerto predeterminado es 5432; POSTGRES_PORT cambia el puerto publicado. Base, usuario y contraseña local: coworkhub.

Los comandos Docker también funcionan en Linux/macOS. Ejecuta Maven con ./mvnw y el argumento -Dspring-boot.run.profiles=mysql o postgres.

### Operaciones de Docker

| Acción | Comando |
|---|---|
| Ver contenedores | docker compose --profile mysql ps |
| Consultar logs | docker compose --profile mysql logs -f mysql |
| Detener | docker compose --profile mysql stop |
| Iniciar de nuevo | docker compose --profile mysql start |
| Borrar contenedor y red | docker compose --profile mysql down |
| Borrar también los datos | docker compose --profile mysql down -v |

Reemplaza mysql por postgres para PostgreSQL. El último comando elimina permanentemente los datos del volumen.

> **Solo desarrollo:** Compose define contraseñas simples. No publiques esos puertos ni reutilices esas contraseñas en un servidor o entorno productivo.

### Esquema y datos iniciales

Los perfiles H2, MySQL y PostgreSQL usan spring.jpa.hibernate.ddl-auto=create: cada arranque recrea las tablas. Hibernate carga los catálogos, usuarios y miembros desde src/main/resources/import.sql; CargadorReservasDemo genera reservas con fechas relativas al reloj configurado. Al reiniciar se pierden los cambios y se vuelve al conjunto inicial.

## Configuración

La configuración de desarrollo está en src/main/resources/application*.properties y docker-compose.yml. Se puede sobrescribir con variables de entorno:

| Variable | Uso | Valor local |
|---|---|---|
| SPRING_PROFILES_ACTIVE | Perfil h2, mysql o postgres | h2 |
| COWORKHUB_DB_HOST | Host de la base | localhost |
| COWORKHUB_DB_PORT | Puerto MySQL/PostgreSQL | 3306 / 5432 |
| COWORKHUB_DB_NOMBRE | Nombre de la base | coworkhub |
| COWORKHUB_DB_USUARIO | Usuario de la base | coworkhub |
| COWORKHUB_DB_CONTRASENA | Contraseña de la base | coworkhub |
| COWORKHUB_SEGURIDAD_JWT_CLAVE | Clave JWT de al menos 32 caracteres | Clave educativa de properties |
| COWORKHUB_RELOJ_FIJO | Fijar hora para pruebas reproducibles | 2030-06-03T08:00:00 |

Ejemplo PowerShell:

~~~powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:COWORKHUB_DB_USUARIO = 'coworkhub'
$env:COWORKHUB_DB_CONTRASENA = 'coworkhub'
$env:COWORKHUB_SEGURIDAD_JWT_CLAVE = 'cambiar-por-una-clave-local-de-al-menos-32-caracteres'
.\mvnw.cmd spring-boot:run
~~~

Ejemplo Linux/macOS:

~~~bash
export SPRING_PROFILES_ACTIVE=mysql
export COWORKHUB_SEGURIDAD_JWT_CLAVE='cambiar-por-una-clave-local-de-al-menos-32-caracteres'
./mvnw spring-boot:run
~~~

Las variables de Compose MYSQL_PORT y POSTGRES_PORT se configuran antes de ejecutar Docker. No guardes claves reales ni contraseñas privadas en el repositorio.

## Acceso a la aplicación

| Dirección | Contenido |
|---|---|
| http://localhost:8080/ | Interfaz web |
| http://localhost:8080/doc/swagger-ui.html | Swagger UI |
| http://localhost:8080/v3/api-docs | OpenAPI generado |
| [docs/openapi.json](docs/openapi.json) | OpenAPI versionado |

Para iniciar sesión envía POST /auth/login con el JSON {"nombreUsuario":"admin","contrasena":"admin123"}. Copia el campo token. En Swagger pulsa Authorize y pega el token; en Postman usa Bearer Token o la colección incluida.

## Usuarios de demostración

| Usuario | Contraseña | Rol | Alcance |
|---|---|---|---|
| admin | admin123 | ADMIN | Administra catálogos, miembros y reservas |
| recepcion | recep123 | RECEPCION | Gestiona miembros y reservas |
| ana | ana123 | MIEMBRO | Ana Torres, miembro 1 |
| luis | luis123 | MIEMBRO | Luis Gómez, miembro 2 |

Las contraseñas se almacenan como hashes BCrypt en los datos iniciales. La tabla muestra las claves de demostración en texto para iniciar sesión en la instancia local. Marta, Carlos y Sofía son miembros sin cuenta; Sofía está suspendida para probar reglas de negocio.

## Roles

| Rol | Permisos principales |
|---|---|
| ADMIN | Administra recursos y opera reservas |
| RECEPCION | Gestiona miembros y reservas de cualquier miembro |
| MIEMBRO | Consulta catálogos y disponibilidad; opera sus reservas y consumo |

Las rutas protegidas requieren Authorization: Bearer <token>. La API responde 401 si falta un token válido y 403 si el usuario no tiene permiso.

## API y rutas

| Recurso | Rutas principales |
|---|---|
| Autenticación | POST /auth/login |
| Sedes | /api/sedes: GET, GET /{id}, POST, PUT /{id}, DELETE /{id} |
| Salas | /api/salas: operaciones CRUD |
| Equipamientos | /api/equipamientos: operaciones CRUD |
| Servicios | /api/servicios-adicionales: operaciones CRUD |
| Planes | /api/planes: operaciones CRUD |
| Miembros | /api/miembros, /{id}, POST, PUT /{id}, PATCH /{id}/suspender, PATCH /{id}/activar, DELETE /{id}, /{id}/consumo?mes=yyyy-MM |
| Disponibilidad | GET /api/disponibilidad?sedeId=&fecha=&horaInicio=&horaFin=&capacidadMinima=&equipamientoIds= |
| Reservas | POST /api/reservas; GET /api/reservas/{id}; listado con miembroId, salaId + fecha o estado; acciones confirmar, cancelar, completar y servicios |

Los errores comparten los campos error, codigo y timestamp. Estados habituales: 400 solicitud inválida, 401 no autenticado, 403 sin permiso, 404 recurso inexistente y 409 regla de negocio (por ejemplo, sala ocupada).

## Postman

Importa la colección con **14 escenarios**: [postman/CoWorkHub.postman_collection.json](postman/CoWorkHub.postman_collection.json). En Postman usa **Import → File** y selecciona el JSON. La variable base_url apunta por defecto a http://localhost:8080.

1. Arranca la aplicación y espera a que carguen los datos.
2. Ejecuta las cuatro solicitudes de inicio de sesión en 00 - Autenticación. Cada una guarda el JWT en una variable de colección.
3. Ejecuta los escenarios 01 a 14 en orden. Para el escenario de concurrencia, envía sus dos solicitudes a la vez desde dos pestañas: se espera un 201 y un 409.
4. Reinicia la aplicación para restaurar los datos iniciales después de escenarios que cancelan reservas o crean registros.

La colección contiene assertions de estado HTTP y de código de error cuando aplica. No guardes tokens reales en la colección ni en el repositorio.

## Pruebas

~~~powershell
.\mvnw.cmd test
~~~

En Linux/macOS usa ./mvnw test. La colección Postman verifica escenarios de aceptación; no reemplaza las pruebas de Maven.

## Estructura del proyecto

~~~text
config/                         Beans, OpenAPI y carga de reservas demo
negocio/model/                  Entidades y estados del dominio
negocio/service/                Reglas de negocio y cálculos
persistence/repository/         Repositorios Spring Data JPA
presentacion/controlador/       Endpoints REST
presentacion/dto/               Solicitudes y respuestas HTTP
security/                       JWT, autenticación y permisos
src/main/resources/
  application*.properties       Configuración de perfiles
  import.sql                    Catálogos, usuarios y miembros de ejemplo
  static/                       Interfaz web (HTML, CSS y JavaScript)
postman/                        Colección de 14 escenarios
docs/                           OpenAPI, análisis y colección Insomnia
docker-compose.yml              Perfiles MySQL y PostgreSQL
~~~

## Seguridad y datos

Este repositorio es educativo. Incluye deliberadamente una clave JWT de desarrollo en src/main/resources/application.properties, contraseñas sencillas para las bases Docker y cuentas de demostración con contraseñas conocidas. La clave y las credenciales tienen advertencia explícita en la configuración y aquí: **son solo para desarrollo; no son aptas para producción**. La clave JWT y las credenciales de base de datos se pueden sobrescribir con variables de entorno.

Los nombres, documentos y correos de data.sql e import.sql son datos sintéticos para pruebas; los correos usan el dominio reservado .test. No encontré indicios de datos personales reales en las semillas revisadas. Los hashes BCrypt de los archivos SQL son hashes de las contraseñas de demostración listadas arriba; no equivalen a guardar las claves en claro, pero las contraseñas conocidas solo sirven localmente.

Antes de desplegar, genera una clave JWT aleatoria de al menos 32 caracteres, usa credenciales fuertes fuera del repositorio y reemplaza las cuentas/datos de prueba. Si una clave real fue incluida en cualquier commit, rótala aunque luego se haya borrado del archivo.

## Documentación adicional

- [Análisis del proyecto](docs/analisis.md): actores, casos de uso, modelo y reglas.
- [OpenAPI versionado](docs/openapi.json).
- [Colección Insomnia](docs/insomnia-coworkhub.json).
- [Clase principal](src/main/java/com/coworkhub/Main.java).
