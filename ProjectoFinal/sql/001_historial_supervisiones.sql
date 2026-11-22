-- =========================================================
-- Historial de supervisiones
-- Antes: UNIQUE (id_empleado) -> un empleado solo podía tener UNA fila,
--        así que al cambiar de jefe se perdía el anterior.
-- Ahora: se guardan todas las supervisiones (historial) y solo se
--        impide que un empleado tenga DOS jefes activos a la vez.
--
-- Ejecutar una sola vez sobre la base sistema_atencion.
-- =========================================================

BEGIN;

ALTER TABLE supervisiones
    DROP CONSTRAINT IF EXISTS supervisiones_id_empleado_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_supervisiones_empleado_activo
    ON supervisiones (id_empleado)
    WHERE activo = true;

COMMIT;
