# Análisis de CoWorkHub

## 1. Actores

| Actor | Responsabilidad |
|---|---|
| `ADMIN` | Administra sedes, salas, equipamientos, servicios, planes y miembros. También consulta y opera reservas. |
| `RECEPCION` | Opera reservas: consulta, confirma, cancela y completa reservas. |
| `MIEMBRO` | Consulta su información y consumo, crea reservas propias y consulta únicamente sus reservas. |

Todas las solicitudes protegidas usan JWT stateless. El token contiene el rol y se valida en cada solicitud.

## 2. Casos de uso

| ID | Caso de uso | Actor principal | Endpoint |
|---|---|---|---|
| CU-01 | Iniciar sesión | Todos | `POST /auth/login` |
| CU-02 | Consultar disponibilidad | Miembro, recepción, admin | `GET /api/disponibilidad` |
| CU-03 | Crear reserva | Miembro, recepción, admin | `POST /api/reservas` |
| CU-04 | Consultar reservas | Dueño, recepción, admin | `GET /api/reservas`, `GET /api/reservas/{id}` |
| CU-05 | Modificar servicios | Dueño, recepción, admin | `POST/DELETE /api/reservas/{id}/servicios` |
| CU-06 | Cambiar estado de reserva | Recepción, admin | `POST /confirmar`, `/cancelar`, `/completar` |
| CU-07 | Gestionar sedes | Admin | `/api/sedes` |
| CU-08 | Gestionar salas | Admin | `/api/salas` |
| CU-09 | Gestionar equipamientos | Admin | `/api/equipamientos` |
| CU-10 | Gestionar servicios adicionales | Admin | `/api/servicios-adicionales` |
| CU-11 | Gestionar planes | Admin | `/api/planes` |
| CU-12 | Gestionar miembros | Admin | `/api/miembros` |
| CU-13 | Consultar consumo | Miembro, recepción, admin | `GET /api/miembros/{id}/consumo` |

## 3. Glosario

- **Reserva:** intervalo de uso de una sala para un miembro, con asistentes, estado, costos y servicios adicionales.
- **Plan:** configuración de membresía con horas incluidas, descuento de excedentes y máximo de reservas activas.
- **Horas incluidas:** minutos del mes cubiertos por el plan antes de calcular cobro por excedente.
- **Horas excedentes:** minutos que superan las horas incluidas; se cobran con el descuento del plan.
- **Solapamiento:** dos intervalos de la misma sala se cruzan cuando `inicioNuevo < finExistente` y `finNuevo > inicioExistente`. Los extremos que se tocan sí se permiten.
- **Cargo por cancelación:** cero si faltan al menos 24 horas; 50% del costo de sala si faltan entre 2 y menos de 24 horas; no se permite cancelar con menos de 2 horas o una vez iniciada.

## 4. Modelo entidad-relación y decisiones JPA

Entidades: `Sede`, `Sala`, `Equipamiento`, `ServicioAdicional`, `PlanMembresia`, `Miembro`, `Usuario`, `Reserva` y `DetalleReserva`.

| Relación | Dueño | Fetch/cascade | Justificación |
|---|---|---|---|
| `Sede 1:N Sala` | `Sala.sede` | Colección inversa LAZY | La FK `sede_id` pertenece a `sala`; una sede no necesita cargar todas sus salas. |
| `Sala N:N Equipamiento` | `Sala.equipamientos` | Colecciones LAZY | `Sala` declara la tabla `sala_equipamiento`; se carga explícitamente en listados mediante `EntityGraph`. |
| `Miembro N:1 PlanMembresia` | `Miembro.plan` | Asociación necesaria | El miembro guarda `plan_id`; el plan no se elimina en cascada porque es catálogo compartido. |
| `Miembro 1:1 Usuario` | `Miembro.usuario` | LAZY, `ALL`, `orphanRemoval` | El miembro guarda `usuario_id`; el usuario pertenece al miembro y no se expone en JSON. |
| `Reserva N:1 Sala` | `Reserva.sala` | Asociación necesaria | La reserva guarda `sala_id` y necesita la sala para validar disponibilidad. |
| `Reserva N:1 Miembro` | `Reserva.miembro` | Asociación necesaria | La reserva guarda `miembro_id` y necesita el plan y dueño. |
| `Reserva 1:N DetalleReserva` | `DetalleReserva.reserva` / colección en Reserva | LAZY, `CascadeType.ALL`, `orphanRemoval` | Los detalles no tienen sentido sin la reserva; quitar un servicio elimina su fila. |
| `DetalleReserva N:1 ServicioAdicional` | `DetalleReserva.servicio` | Sin borrado en cascada | El detalle conserva `precioUnitarioAplicado`, por lo que el cambio futuro de precio no modifica el historial. |

Las colecciones se serializan sin ciclos mediante `@JsonIgnore` en los lados inversos. Los listados de salas, miembros y reservas usan `EntityGraph` para evitar N+1 en las relaciones necesarias.

## 5. Matriz de trazabilidad

| RF | Descripción | RN relacionada | Endpoint |
|---|---|---|---|
| RF-01 | Consultar catálogos y disponibilidad | RN-03, RN-04 | `GET /api/sedes`, `/salas`, `/disponibilidad` |
| RF-02 | Crear y actualizar miembros | RN-05, RN-11, RN-12 | `POST/PUT /api/miembros` |
| RF-03 | Consultar disponibilidad | RN-01, RN-03, RN-04 | `GET /api/disponibilidad` |
| RF-04 | Crear reserva | RN-01 a RN-07 | `POST /api/reservas` |
| RF-05 | Consultar reservas | RN-13 | `GET /api/reservas`, `GET /api/reservas/{id}` |
| RF-06 | Agregar/quitar servicios | RN-07, RN-10 | `POST/DELETE /api/reservas/{id}/servicios` |
| RF-07 | Confirmar y completar | RN-09 | `POST /confirmar`, `/completar` |
| RF-08 | Cancelar | RN-08, RN-09 | `POST /cancelar` |
| RF-09 | Administrar catálogos | RN-11, RN-12, RN-13 | CRUD de sedes, salas, equipamientos, servicios y planes |
| RF-10 | Consultar consumo | RN-07 | `GET /api/miembros/{id}/consumo` |
| RF-11 | Autenticación/autorización | RN-13 | `POST /auth/login` y JWT |

## 6. Supuestos y decisiones

- Una reserva debe durar entre 30 minutos y 8 horas, en múltiplos de 30 minutos.
- La reserva debe estar dentro del horario de su sede y solicitarse con mínimo una hora de anticipación.
- Las reservas `PENDIENTE` y `CONFIRMADA` ocupan sala y cuentan como activas.
- Una cancelación con 24 horas o más devuelve las horas consumidas; una cancelación tardía conserva el consumo y cobra 50% del costo de sala.
- La concurrencia se resuelve bloqueando pesimistamente la fila de la sala durante la creación.
- El escenario del enunciado que dice `SUSPENDED` se representa en este código como el enum español `SUSPENDIDO`.
- El H2 de desarrollo recrea el esquema y carga `import.sql` en cada arranque.

## 7. Fuera de alcance

- Pagos reales y facturación electrónica.
- Notificaciones por correo o mensajería.
- Integración con proveedores externos de identidad.
- Gestión multiempresa, tarifas por temporada y zonas horarias distintas.
- Panel de auditoría y reportes financieros avanzados.
