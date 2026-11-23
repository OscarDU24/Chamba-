-- =========================================================
-- Integridad de datos
--  1. Una sola evaluación INDIVIDUAL por empleado y período,
--     y una sola DEPARTAMENTAL por departamento y período.
--  2. Correo institucional único sin distinguir mayúsculas
--     (ADMIN@... y admin@... cuentan como el mismo).
--  3. Seguimientos coherentes con su asignación:
--     - la fecha cae dentro del período de la asignación;
--     - un seguimiento DEPARTAMENTAL es del departamento de la asignación;
--     - un seguimiento INDIVIDUAL es de un empleado que participa en ella.
--
-- Ejecutar después de 001, 002 y 003. Se puede ejecutar más de una vez.
-- Si los datos actuales no cumplen alguna regla, el script se detiene
-- con un mensaje y no cambia nada.
-- =========================================================

BEGIN;

-- ---------------------------------------------------------
-- Revisión previa: los datos actuales deben cumplir las reglas
-- ---------------------------------------------------------
DO $$
DECLARE
    n integer;
BEGIN
    SELECT count(*) INTO n FROM (
        SELECT 1 FROM evaluaciones WHERE tipo = 'INDIVIDUAL'
        GROUP BY id_periodo, id_empleado HAVING count(*) > 1) x;
    IF n > 0 THEN
        RAISE EXCEPTION 'Hay % empleado(s) con más de una evaluación individual en el mismo período. Corrígelo antes de aplicar este script.', n;
    END IF;

    SELECT count(*) INTO n FROM (
        SELECT 1 FROM evaluaciones WHERE tipo = 'DEPARTAMENTAL'
        GROUP BY id_periodo, id_departamento HAVING count(*) > 1) x;
    IF n > 0 THEN
        RAISE EXCEPTION 'Hay % departamento(s) con más de una evaluación departamental en el mismo período.', n;
    END IF;

    SELECT count(*) INTO n FROM (
        SELECT 1 FROM empleados GROUP BY lower(correo_institucional) HAVING count(*) > 1) x;
    IF n > 0 THEN
        RAISE EXCEPTION 'Hay % correo(s) repetidos que solo cambian en mayúsculas/minúsculas.', n;
    END IF;

    SELECT count(*) INTO n
    FROM seguimientos s
    JOIN objetivos_departamento a ON a.id_asignacion = s.id_asignacion
    JOIN periodos_evaluacion p ON p.id_periodo = a.id_periodo
    WHERE s.fecha_seguimiento NOT BETWEEN p.fecha_inicio AND p.fecha_fin
       OR (s.tipo = 'DEPARTAMENTAL' AND s.id_departamento <> a.id_departamento)
       OR (s.tipo = 'INDIVIDUAL' AND NOT EXISTS (
              SELECT 1 FROM responsables_objetivos r
              WHERE r.id_asignacion = s.id_asignacion AND r.id_empleado = s.id_empleado));
    IF n > 0 THEN
        RAISE EXCEPTION 'Hay % seguimiento(s) que no cumplen las reglas (fecha fuera del período, departamento distinto o empleado que no participa).', n;
    END IF;
END $$;

-- ---------------------------------------------------------
-- 1. Una evaluación por evaluado y período
-- ---------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS ux_evaluaciones_individual
    ON evaluaciones (id_periodo, id_empleado)
    WHERE tipo = 'INDIVIDUAL';

CREATE UNIQUE INDEX IF NOT EXISTS ux_evaluaciones_departamental
    ON evaluaciones (id_periodo, id_departamento)
    WHERE tipo = 'DEPARTAMENTAL';

-- ---------------------------------------------------------
-- 2. Correo único sin distinguir mayúsculas
-- ---------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS ux_empleados_correo_minusculas
    ON empleados (lower(correo_institucional));

-- ---------------------------------------------------------
-- 3. Seguimientos coherentes con su asignación
-- ---------------------------------------------------------
CREATE OR REPLACE FUNCTION validar_seguimiento() RETURNS trigger AS $$
DECLARE
    v_inicio date;
    v_fin date;
    v_departamento uuid;
BEGIN
    SELECT p.fecha_inicio, p.fecha_fin, a.id_departamento
      INTO v_inicio, v_fin, v_departamento
    FROM objetivos_departamento a
    JOIN periodos_evaluacion p ON p.id_periodo = a.id_periodo
    WHERE a.id_asignacion = NEW.id_asignacion;

    IF NEW.fecha_seguimiento < v_inicio OR NEW.fecha_seguimiento > v_fin THEN
        RAISE EXCEPTION 'La fecha del seguimiento (%) está fuera del período (% a %)',
            NEW.fecha_seguimiento, v_inicio, v_fin
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.tipo = 'DEPARTAMENTAL' AND NEW.id_departamento <> v_departamento THEN
        RAISE EXCEPTION 'El seguimiento departamental debe ser del departamento de la asignación'
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.tipo = 'INDIVIDUAL' AND NOT EXISTS (
            SELECT 1 FROM responsables_objetivos r
            WHERE r.id_asignacion = NEW.id_asignacion AND r.id_empleado = NEW.id_empleado) THEN
        RAISE EXCEPTION 'El empleado no participa en el objetivo de este seguimiento'
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_seguimientos_validar ON seguimientos;
CREATE TRIGGER trg_seguimientos_validar
    BEFORE INSERT OR UPDATE ON seguimientos
    FOR EACH ROW EXECUTE FUNCTION validar_seguimiento();

COMMIT;
