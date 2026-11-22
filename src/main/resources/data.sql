-- Datos iniciales de catálogos (RNF-09).
-- Spring Boot ejecuta este script al arrancar, DESPUÉS de que Hibernate crea las tablas
-- (ver spring.jpa.defer-datasource-initialization en application.properties).
-- Las claves foráneas se resuelven con subconsultas por nombre, para no depender de los ids.

-- ============================================================ Sedes
INSERT INTO sede (nombre, ciudad, direccion, hora_apertura, hora_cierre) VALUES
    ('Sede Centro', 'Bogotá',   'Carrera 7 # 32-16', '07:00:00', '22:00:00'),
    ('Sede Norte',  'Medellín', 'Calle 10 # 43-20',  '08:00:00', '20:00:00');

-- ============================================================ Equipamiento
INSERT INTO equipamiento (nombre) VALUES
    ('Proyector'),
    ('Pizarra'),
    ('Videoconferencia'),
    ('Aire acondicionado');

-- ============================================================ Salas
-- Escritorio B1 está inactiva a propósito, para probar RN-04.
INSERT INTO sala (nombre, tipo, capacidad, tarifa_por_hora, activa, sede_id) VALUES
    ('Sala Andes',    'SALA_REUNION',    8,  30.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Sala Caribe',   'SALA_REUNION',    4,  20.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Oficina 101',   'OFICINA_PRIVADA', 3,  15.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Escritorio A1', 'ESCRITORIO',      1,   5.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Centro')),
    ('Sala Pacífico', 'SALA_REUNION',    12, 40.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Norte')),
    ('Oficina 201',   'OFICINA_PRIVADA', 4,  18.00, TRUE,  (SELECT id FROM sede WHERE nombre = 'Sede Norte')),
    ('Escritorio B1', 'ESCRITORIO',      1,   5.00, FALSE, (SELECT id FROM sede WHERE nombre = 'Sede Norte'));

-- Equipamiento de cada sala (tabla intermedia de la relación muchos a muchos)
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Sala Andes' AND e.nombre IN ('Proyector', 'Pizarra', 'Videoconferencia');
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Sala Caribe' AND e.nombre = 'Pizarra';
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Oficina 101' AND e.nombre = 'Aire acondicionado';
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Sala Pacífico' AND e.nombre IN ('Proyector', 'Pizarra', 'Videoconferencia', 'Aire acondicionado');
INSERT INTO sala_equipamiento (sala_id, equipamiento_id)
    SELECT s.id, e.id FROM sala s, equipamiento e
    WHERE s.nombre = 'Oficina 201' AND e.nombre = 'Pizarra';

-- ============================================================ Servicios adicionales
INSERT INTO servicio_adicional (nombre, precio_unitario) VALUES
    ('Catering',        25.00),
    ('Impresión',        5.00),
    ('Soporte técnico', 40.00);

-- ============================================================ Planes de membresía
-- nombre, horas incluidas al mes, % de descuento en horas excedentes, máximo de reservas activas
INSERT INTO plan_membresia (nombre, horas_incluidas_mes, descuento_excedente, max_reservas_activas) VALUES
    ('Flex',         0,  0, 2),
    ('Básico',      10, 10, 3),
    ('Profesional', 40, 20, 5),
    ('Corporativo', 120, 30, 10);

-- ============================================================ Usuarios
-- Las contraseñas están codificadas con BCrypt (nunca en texto plano):
--   admin123 · recep123 · ana123 · luis123
INSERT INTO usuario (nombre_usuario, contrasena, rol) VALUES
    ('admin',     '$2a$10$39hh5RkT67Qi1SIf.dAnR.WiiX5RzSm9YweaAveBaa9vAOlR1mm/C', 'ADMIN'),
    ('recepcion', '$2a$10$MDhaCnzClH1kMKofpB.FauB/PKiKhl5ijGPL883MHJ5s/5Eq9X2PK', 'RECEPCION'),
    ('ana',       '$2a$10$Oa2oRCfTWRe0X9Lxd1yU3er.aYHdSYRqKSuqXUs07sOYcJzS7hZlu', 'MIEMBRO'),
    ('luis',      '$2a$10$A.WYqrDC1F7hlx66LOB10e096ni/0MVq/oEdUak35V8aXUXgf9y22', 'MIEMBRO');

-- ============================================================ Miembros
-- Ana y Luis tienen usuario (pueden iniciar sesión). Sofía está suspendida a propósito, para probar RN-05.
INSERT INTO miembro (documento, nombre, email, estado, plan_id, usuario_id) VALUES
    ('1010101', 'Ana Torres',  'ana@coworkhub.test',    'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Profesional'),
        (SELECT id FROM usuario WHERE nombre_usuario = 'ana')),
    ('2020202', 'Luis Gómez',  'luis@coworkhub.test',   'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Básico'),
        (SELECT id FROM usuario WHERE nombre_usuario = 'luis')),
    ('3030303', 'Marta Ríos',  'marta@coworkhub.test',  'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Flex'), NULL),
    ('4040404', 'Carlos Peña', 'carlos@coworkhub.test', 'ACTIVO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Corporativo'), NULL),
    ('5050505', 'Sofía León',  'sofia@coworkhub.test',  'SUSPENDIDO',
        (SELECT id FROM plan_membresia WHERE nombre = 'Básico'), NULL);