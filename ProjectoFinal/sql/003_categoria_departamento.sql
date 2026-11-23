-- =========================================================
-- Regla categoría -> departamento
--  * DOCENTES              -> facultades
--  * MANTENIMIENTO         -> Mantenimiento (general)
--  * SEGURIDAD             -> Seguridad
--  * SERVICIOS_ESTUDIANTES -> Cartera y Cobro, Registro Académico, Bienestar Estudiantil
--  * OTROS                 -> sin restricción (no tiene filas en categoria_departamento)
--
-- Una categoría SIN filas en categoria_departamento puede ir en cualquier
-- departamento (o sin departamento). Una categoría CON filas solo puede ir
-- en esos departamentos y el departamento es obligatorio.
--
-- Ejecutar después de 001 y 002. Se puede ejecutar más de una vez.
-- Cuentas de ejemplo nuevas (rol EMPLEADO): contraseña Empleado123
-- =========================================================

BEGIN;

-- 1. Departamentos nuevos
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Facultad de Medicina', 'Formación médica y ciencias de la salud.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Facultad de Medicina');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Facultad de Odontología', 'Formación en odontología con apoyo de clínicas odontológicas.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Facultad de Odontología');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Facultad de Ciencias Administrativas y Económicas', 'Administración de Empresas, Contabilidad y Finanzas, Negocios Internacionales y Economía.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Facultad de Ciencias Administrativas y Económicas');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Facultad de Marketing, Diseño y Ciencias de la Comunicación', 'Marketing y Publicidad, Diseño y Comunicación Visual, y Comunicación y Relaciones Públicas.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Facultad de Marketing, Diseño y Ciencias de la Comunicación');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Facultad de Ingeniería y Arquitectura', 'Arquitectura, Ingeniería Civil, Ingeniería Industrial e Ingeniería en Sistemas de Información.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Facultad de Ingeniería y Arquitectura');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Facultad de Ciencias Jurídicas, Humanidades y Relaciones Internacionales', 'Derecho, y Diplomacia y Relaciones Internacionales.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Facultad de Ciencias Jurídicas, Humanidades y Relaciones Internacionales');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'UAM College', 'Programas impartidos en inglés bajo un modelo basado en créditos.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'UAM College');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Mantenimiento', 'Mantenimiento general de las instalaciones de la institución.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Mantenimiento');
INSERT INTO departamentos (nombre, descripcion, activo)
SELECT 'Seguridad', 'Seguridad institucional.', true
WHERE NOT EXISTS (SELECT 1 FROM departamentos WHERE nombre = 'Seguridad');

-- 2. Tabla de combinaciones permitidas
CREATE TABLE IF NOT EXISTS categoria_departamento (
    id_categoria    uuid NOT NULL REFERENCES categorias_personal (id_categoria),
    id_departamento uuid NOT NULL REFERENCES departamentos (id_departamento),
    PRIMARY KEY (id_categoria, id_departamento)
);

-- 3. Regla acordada por el equipo
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'Facultad de Medicina'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'Facultad de Odontología'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'Facultad de Ciencias Administrativas y Económicas'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'Facultad de Marketing, Diseño y Ciencias de la Comunicación'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'Facultad de Ingeniería y Arquitectura'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'Facultad de Ciencias Jurídicas, Humanidades y Relaciones Internacionales'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'DOCENTES' AND d.nombre = 'UAM College'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'MANTENIMIENTO' AND d.nombre = 'Mantenimiento'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'SEGURIDAD' AND d.nombre = 'Seguridad'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'SERVICIOS_ESTUDIANTES' AND d.nombre = 'Cartera y Cobro'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'SERVICIOS_ESTUDIANTES' AND d.nombre = 'Registro Académico'
ON CONFLICT DO NOTHING;
INSERT INTO categoria_departamento (id_categoria, id_departamento)
SELECT c.id_categoria, d.id_departamento FROM categorias_personal c, departamentos d
WHERE c.nombre = 'SERVICIOS_ESTUDIANTES' AND d.nombre = 'Bienestar Estudiantil'
ON CONFLICT DO NOTHING;

-- 4. Corregir datos de ejemplo de 002 que no cumplían la regla
--    (eran DOCENTES en departamentos de servicios al estudiante)
UPDATE empleados SET id_categoria = (SELECT id_categoria FROM categorias_personal WHERE nombre = 'SERVICIOS_ESTUDIANTES')
WHERE cif IN ('EMP-005', 'EMP-008')
  AND id_categoria = (SELECT id_categoria FROM categorias_personal WHERE nombre = 'DOCENTES');

-- 5. Trigger: la base rechaza combinaciones no permitidas
CREATE OR REPLACE FUNCTION validar_categoria_departamento() RETURNS trigger AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM categoria_departamento WHERE id_categoria = NEW.id_categoria) THEN
        IF NEW.id_departamento IS NULL OR NOT EXISTS (
            SELECT 1 FROM categoria_departamento
            WHERE id_categoria = NEW.id_categoria AND id_departamento = NEW.id_departamento) THEN
            RAISE EXCEPTION 'El departamento no corresponde a la categoría del empleado'
                USING ERRCODE = 'check_violation';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_empleados_categoria_departamento ON empleados;
CREATE TRIGGER trg_empleados_categoria_departamento
    BEFORE INSERT OR UPDATE OF id_categoria, id_departamento ON empleados
    FOR EACH ROW EXECUTE FUNCTION validar_categoria_departamento();

-- 6. Empleados de ejemplo en las áreas nuevas
INSERT INTO empleados (nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo)
SELECT 'Roberto Carlos Mendoza Aguilar', 'EMP-010', 'roberto.mendoza@institucion.edu',
       (SELECT id_categoria FROM categorias_personal WHERE nombre = 'DOCENTES'),
       (SELECT id_departamento FROM departamentos WHERE nombre = 'Facultad de Ingeniería y Arquitectura'), true
ON CONFLICT DO NOTHING;
INSERT INTO cuentas_usuario (id_empleado, contrasena_hash, rol, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-010'), '210000:041p3m21m4n/3Goglm+8kw==:EHmM2p3b6egdU4vTquAHDN8uWasYAceJRynGHUq68rk=', 'EMPLEADO', true
ON CONFLICT DO NOTHING;
INSERT INTO empleados (nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo)
SELECT 'Karla Vanessa Ortiz Blandón', 'EMP-011', 'karla.ortiz@institucion.edu',
       (SELECT id_categoria FROM categorias_personal WHERE nombre = 'DOCENTES'),
       (SELECT id_departamento FROM departamentos WHERE nombre = 'Facultad de Ingeniería y Arquitectura'), true
ON CONFLICT DO NOTHING;
INSERT INTO cuentas_usuario (id_empleado, contrasena_hash, rol, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-011'), '210000:1mvO3DhzxFYAfqlrCwyRjA==:hYN5dsYHGH4jD3vc+YT4153InkhO8MV4oV/oUG8zkRQ=', 'EMPLEADO', true
ON CONFLICT DO NOTHING;
INSERT INTO empleados (nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo)
SELECT 'Fernando José Castillo Reyes', 'EMP-012', 'fernando.castillo@institucion.edu',
       (SELECT id_categoria FROM categorias_personal WHERE nombre = 'DOCENTES'),
       (SELECT id_departamento FROM departamentos WHERE nombre = 'Facultad de Medicina'), true
ON CONFLICT DO NOTHING;
INSERT INTO cuentas_usuario (id_empleado, contrasena_hash, rol, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-012'), '210000:LyKiYquU6f4IbD3p4adEMQ==:A+m2QcGSE8QmN60BxcN3tKiNXcmtjXb1FK6skhR4560=', 'EMPLEADO', true
ON CONFLICT DO NOTHING;
INSERT INTO empleados (nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo)
SELECT 'Pedro Antonio Rivas López', 'EMP-013', 'pedro.rivas@institucion.edu',
       (SELECT id_categoria FROM categorias_personal WHERE nombre = 'MANTENIMIENTO'),
       (SELECT id_departamento FROM departamentos WHERE nombre = 'Mantenimiento'), true
ON CONFLICT DO NOTHING;
INSERT INTO cuentas_usuario (id_empleado, contrasena_hash, rol, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-013'), '210000:me2rOgqFZuxoFm93Y4afPg==:bz9dbG009nocSCzbagrzUseiFdhk16dO3yhfeftNeIU=', 'EMPLEADO', true
ON CONFLICT DO NOTHING;
INSERT INTO empleados (nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo)
SELECT 'Juan Carlos Morales Pineda', 'EMP-014', 'juan.morales@institucion.edu',
       (SELECT id_categoria FROM categorias_personal WHERE nombre = 'SEGURIDAD'),
       (SELECT id_departamento FROM departamentos WHERE nombre = 'Seguridad'), true
ON CONFLICT DO NOTHING;
INSERT INTO cuentas_usuario (id_empleado, contrasena_hash, rol, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-014'), '210000:oHezPZypDt7misTi7knxRQ==:vpPb4YRzBFoeK/TqSE3eZc4+RCHTtZlPOX1S7u7bt+4=', 'EMPLEADO', true
ON CONFLICT DO NOTHING;
INSERT INTO supervisiones (id_empleado, id_jefe, fecha_inicio, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-010'), (SELECT id_empleado FROM empleados WHERE cif = 'ADMIN-001'), DATE '2026-01-10', true
WHERE NOT EXISTS (SELECT 1 FROM supervisiones WHERE id_empleado = (SELECT id_empleado FROM empleados WHERE cif = 'EMP-010') AND activo = true);
INSERT INTO supervisiones (id_empleado, id_jefe, fecha_inicio, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-011'), (SELECT id_empleado FROM empleados WHERE cif = 'EMP-010'), DATE '2026-01-10', true
WHERE NOT EXISTS (SELECT 1 FROM supervisiones WHERE id_empleado = (SELECT id_empleado FROM empleados WHERE cif = 'EMP-011') AND activo = true);
INSERT INTO supervisiones (id_empleado, id_jefe, fecha_inicio, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-012'), (SELECT id_empleado FROM empleados WHERE cif = 'ADMIN-001'), DATE '2026-01-10', true
WHERE NOT EXISTS (SELECT 1 FROM supervisiones WHERE id_empleado = (SELECT id_empleado FROM empleados WHERE cif = 'EMP-012') AND activo = true);
INSERT INTO supervisiones (id_empleado, id_jefe, fecha_inicio, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-013'), (SELECT id_empleado FROM empleados WHERE cif = 'ADMIN-001'), DATE '2026-01-10', true
WHERE NOT EXISTS (SELECT 1 FROM supervisiones WHERE id_empleado = (SELECT id_empleado FROM empleados WHERE cif = 'EMP-013') AND activo = true);
INSERT INTO supervisiones (id_empleado, id_jefe, fecha_inicio, activo)
SELECT (SELECT id_empleado FROM empleados WHERE cif = 'EMP-014'), (SELECT id_empleado FROM empleados WHERE cif = 'ADMIN-001'), DATE '2026-01-10', true
WHERE NOT EXISTS (SELECT 1 FROM supervisiones WHERE id_empleado = (SELECT id_empleado FROM empleados WHERE cif = 'EMP-014') AND activo = true);

COMMIT;
