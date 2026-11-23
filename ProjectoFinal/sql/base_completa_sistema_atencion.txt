-- =========================================================
-- BASE COMPLETA sistema_atencion (esquema + datos)
-- Generado desde la base local. Ejecutar en una base VACÍA:
--   CREATE DATABASE sistema_atencion;  (luego conectarse a ella)
-- Ya incluye lo de 001, 002 y 003: NO hace falta correrlos aparte.
-- =========================================================

BEGIN;

-- ===================== TABLAS =====================
CREATE TABLE administradores_sistema (
    id_administrador uuid DEFAULT gen_random_uuid() NOT NULL,
    id_usuario uuid NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT administradores_sistema_id_usuario_key UNIQUE (id_usuario),
    CONSTRAINT administradores_sistema_pkey PRIMARY KEY (id_administrador)
);

CREATE TABLE categoria_departamento (
    id_categoria uuid NOT NULL,
    id_departamento uuid NOT NULL,
    CONSTRAINT categoria_departamento_pkey PRIMARY KEY (id_categoria, id_departamento)
);

CREATE TABLE categorias_personal (
    id_categoria uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT categorias_personal_nombre_key UNIQUE (nombre),
    CONSTRAINT categorias_personal_pkey PRIMARY KEY (id_categoria)
);

CREATE TABLE cuentas_usuario (
    id_usuario uuid DEFAULT gen_random_uuid() NOT NULL,
    id_empleado uuid NOT NULL,
    contrasena_hash character varying(255) NOT NULL,
    rol character varying(20) DEFAULT 'EMPLEADO'::character varying NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT cuentas_usuario_id_empleado_key UNIQUE (id_empleado),
    CONSTRAINT cuentas_usuario_pkey PRIMARY KEY (id_usuario),
    CONSTRAINT chk_rol_usuario CHECK (((rol)::text = ANY ((ARRAY['ADMINISTRADOR'::character varying, 'EMPLEADO'::character varying])::text[])))
);

CREATE TABLE departamentos (
    id_departamento uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    ubicacion character varying(150),
    correo_contacto character varying(120),
    telefono character varying(20),
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT departamentos_nombre_key UNIQUE (nombre),
    CONSTRAINT departamentos_pkey PRIMARY KEY (id_departamento)
);

CREATE TABLE empleados (
    id_empleado uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre_completo character varying(150) NOT NULL,
    cif character varying(120) NOT NULL,
    correo_institucional character varying(120) NOT NULL,
    id_categoria uuid NOT NULL,
    id_departamento uuid,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT empleados_cif_key UNIQUE (cif),
    CONSTRAINT empleados_correo_institucional_key UNIQUE (correo_institucional),
    CONSTRAINT empleados_pkey PRIMARY KEY (id_empleado)
);

CREATE TABLE evaluaciones (
    id_evaluacion uuid DEFAULT gen_random_uuid() NOT NULL,
    id_periodo uuid NOT NULL,
    id_departamento uuid NOT NULL,
    id_empleado uuid,
    tipo character varying(20) NOT NULL,
    puntuacion_individual numeric,
    puntuacion_departamental numeric,
    puntuacion_final numeric,
    observaciones text,
    fecha_evaluacion date DEFAULT CURRENT_DATE NOT NULL,
    evaluado_por uuid NOT NULL,
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    CONSTRAINT evaluaciones_pkey PRIMARY KEY (id_evaluacion),
    CONSTRAINT chk_evaluacion_destino CHECK (((((tipo)::text = 'INDIVIDUAL'::text) AND (id_empleado IS NOT NULL)) OR (((tipo)::text = 'DEPARTAMENTAL'::text) AND (id_empleado IS NULL)))),
    CONSTRAINT chk_evaluacion_estado CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'EN_PROCESO'::character varying, 'FINALIZADA'::character varying])::text[]))),
    CONSTRAINT chk_evaluacion_puntuaciones CHECK ((((puntuacion_individual IS NULL) OR ((puntuacion_individual >= (0)::numeric) AND (puntuacion_individual <= (100)::numeric))) AND ((puntuacion_departamental IS NULL) OR ((puntuacion_departamental >= (0)::numeric) AND (puntuacion_departamental <= (100)::numeric))) AND ((puntuacion_final IS NULL) OR ((puntuacion_final >= (0)::numeric) AND (puntuacion_final <= (100)::numeric))))),
    CONSTRAINT chk_evaluacion_tipo CHECK (((tipo)::text = ANY ((ARRAY['INDIVIDUAL'::character varying, 'DEPARTAMENTAL'::character varying])::text[])))
);

CREATE TABLE objetivos (
    id_objetivo uuid DEFAULT gen_random_uuid() NOT NULL,
    id_objetivo_padre uuid,
    nombre character varying(150) NOT NULL,
    descripcion text,
    nivel character varying(20) NOT NULL,
    indicador character varying(150),
    meta numeric,
    unidad_medida character varying(50),
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT objetivos_pkey PRIMARY KEY (id_objetivo),
    CONSTRAINT chk_objetivo_jerarquia CHECK (((((nivel)::text = 'GENERAL'::text) AND (id_objetivo_padre IS NULL)) OR (((nivel)::text = 'ESPECIFICO'::text) AND (id_objetivo_padre IS NOT NULL)))),
    CONSTRAINT chk_objetivo_meta CHECK (((meta IS NULL) OR (meta >= (0)::numeric))),
    CONSTRAINT chk_objetivo_nivel CHECK (((nivel)::text = ANY ((ARRAY['GENERAL'::character varying, 'ESPECIFICO'::character varying])::text[])))
);

CREATE TABLE objetivos_departamento (
    id_asignacion uuid DEFAULT gen_random_uuid() NOT NULL,
    id_objetivo uuid NOT NULL,
    id_departamento uuid NOT NULL,
    id_periodo uuid NOT NULL,
    fecha_asignacion date DEFAULT CURRENT_DATE NOT NULL,
    CONSTRAINT uq_objetivo_departamento_periodo UNIQUE (id_objetivo, id_departamento, id_periodo),
    CONSTRAINT objetivos_departamento_pkey PRIMARY KEY (id_asignacion)
);

CREATE TABLE periodos_evaluacion (
    id_periodo uuid DEFAULT gen_random_uuid() NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    fecha_inicio date NOT NULL,
    fecha_fin date NOT NULL,
    estado character varying(20) DEFAULT 'PLANIFICADO'::character varying NOT NULL,
    CONSTRAINT periodos_evaluacion_pkey PRIMARY KEY (id_periodo),
    CONSTRAINT chk_periodo_estado CHECK (((estado)::text = ANY ((ARRAY['PLANIFICADO'::character varying, 'EN_CURSO'::character varying, 'CERRADO'::character varying])::text[]))),
    CONSTRAINT chk_periodo_fechas CHECK ((fecha_fin >= fecha_inicio))
);

CREATE TABLE responsables_objetivos (
    id_responsabilidad uuid DEFAULT gen_random_uuid() NOT NULL,
    id_asignacion uuid NOT NULL,
    id_empleado uuid NOT NULL,
    fecha_asignacion date DEFAULT CURRENT_DATE NOT NULL,
    CONSTRAINT uq_responsable_objetivo UNIQUE (id_asignacion, id_empleado),
    CONSTRAINT responsables_objetivos_pkey PRIMARY KEY (id_responsabilidad)
);

CREATE TABLE seguimientos (
    id_seguimiento uuid DEFAULT gen_random_uuid() NOT NULL,
    id_asignacion uuid NOT NULL,
    id_empleado uuid,
    id_departamento uuid,
    fecha_seguimiento date DEFAULT CURRENT_DATE NOT NULL,
    tipo character varying(20) NOT NULL,
    porcentaje_avance numeric NOT NULL,
    observaciones text,
    registrado_por uuid NOT NULL,
    CONSTRAINT seguimientos_pkey PRIMARY KEY (id_seguimiento),
    CONSTRAINT chk_seguimiento_avance CHECK (((porcentaje_avance >= (0)::numeric) AND (porcentaje_avance <= (100)::numeric))),
    CONSTRAINT chk_seguimiento_destino CHECK (((((tipo)::text = 'INDIVIDUAL'::text) AND (id_empleado IS NOT NULL) AND (id_departamento IS NULL)) OR (((tipo)::text = 'DEPARTAMENTAL'::text) AND (id_departamento IS NOT NULL) AND (id_empleado IS NULL)))),
    CONSTRAINT chk_seguimiento_tipo CHECK (((tipo)::text = ANY ((ARRAY['INDIVIDUAL'::character varying, 'DEPARTAMENTAL'::character varying])::text[])))
);

CREATE TABLE supervisiones (
    id_supervision uuid DEFAULT gen_random_uuid() NOT NULL,
    id_empleado uuid NOT NULL,
    id_jefe uuid NOT NULL,
    fecha_inicio date DEFAULT CURRENT_DATE NOT NULL,
    fecha_fin date,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT supervisiones_pkey PRIMARY KEY (id_supervision),
    CONSTRAINT chk_supervision_autoreferencia CHECK ((id_empleado <> id_jefe)),
    CONSTRAINT chk_supervision_fechas CHECK (((fecha_fin IS NULL) OR (fecha_fin >= fecha_inicio)))
);

-- ===================== DATOS =====================
-- administradores_sistema
INSERT INTO administradores_sistema (id_administrador, id_usuario, activo, fecha_creacion) VALUES ('04cbf6b0-3edf-47e5-a225-0b1924c3215e', '75cf01c8-c490-4259-9d54-f52858a2a6ef', 't', '2026-11-18 22:30:20.455167');

-- categoria_departamento
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', '7ace173e-5bc0-40e4-9272-06d2d7772908');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', '52247fda-5d2a-4105-add3-96327dd1a571');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', '590c9b7f-d408-45fc-b2ab-45d1d41b8740');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', '9c785284-727b-40a6-a13f-30aea854461d');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', '734d0915-b988-4eb4-aae0-c743c4a0e4e8');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', '59724e7c-9bf8-4332-b026-39a6e60c01f6');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('1169deac-593a-4963-972e-20e07e755faf', 'c08b10fe-39f1-4936-8445-879b47d03871');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('b372661e-f447-4a39-8d4e-c06f02ff5e70', '88b62d79-3152-4d33-ae97-d04e48e89f1e');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('cda96a4c-2542-4250-bcc8-247f5767cd35', '64f8e8aa-f809-4e9f-997d-7cbed4f68711');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '3ed03317-81b2-401d-8bff-8fca062bff86');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '04f009ce-cf60-4056-a025-1f47569d824f');
INSERT INTO categoria_departamento (id_categoria, id_departamento) VALUES ('7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '5e2f8cb9-155d-4914-844b-26ef4be83b91');

-- categorias_personal
INSERT INTO categorias_personal (id_categoria, nombre, descripcion, activo) VALUES ('1169deac-593a-4963-972e-20e07e755faf', 'DOCENTES', 'Personal encargado de impartir clases en la institución.', 't');
INSERT INTO categorias_personal (id_categoria, nombre, descripcion, activo) VALUES ('b372661e-f447-4a39-8d4e-c06f02ff5e70', 'MANTENIMIENTO', 'Personal encargado del mantenimiento de las instalaciones.', 't');
INSERT INTO categorias_personal (id_categoria, nombre, descripcion, activo) VALUES ('cda96a4c-2542-4250-bcc8-247f5767cd35', 'SEGURIDAD', 'Personal encargado de la seguridad institucional.', 't');
INSERT INTO categorias_personal (id_categoria, nombre, descripcion, activo) VALUES ('7c2df2d4-d6bd-46c4-9692-8eef1dc51382', 'SERVICIOS_ESTUDIANTES', 'Personal encargado de atender y brindar servicios a los estudiantes.', 't');
INSERT INTO categorias_personal (id_categoria, nombre, descripcion, activo) VALUES ('f2b45fee-c22b-4b0b-ba43-da49e1457471', 'OTROS', 'Personal perteneciente a otras áreas institucionales.', 't');

-- cuentas_usuario
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('75cf01c8-c490-4259-9d54-f52858a2a6ef', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '210000:irMharJoqsZ5nOj26lWbIQ==:aPCPJBGnAPaNzRsBWN0yT5xW3w8j+Qb2dhRDIjnzBE8=', 'ADMINISTRADOR', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('12f47bc3-73e3-4225-8c0d-b3401280cb33', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', '210000:VaNiGstjadjMwtryAZrrYg==:ZKeyHJsHToNMkXUpWubNyfL0qaueyxRbbO6s8rebVrg=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('6b6aa48c-69bb-4e5c-8003-23a885411d3c', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', '210000:2GdnBwXprTPF6Tjp+So4ew==:oQn21IOKdQ0BQ6+dX6vMboMwtgvLgRLlbNDj3mrVjA0=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('1aa5be2c-4701-4175-a3c4-3c7c004476f4', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', '210000:bawPBM4uhxflaJCToMMEMA==:A/bhPfrrp2V4zqcGWW95T6rwt0b08MMAw6Ula5sEdaY=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('5ebba722-e232-4821-aeeb-11774ccbd468', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', '210000:zFF3fdyFJCzFEy/7srDpjA==:KrFgIBVIddpNrkJiRC2NLGK2I4O/eWBVjGveOV4bBCw=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('6930d388-85ad-441f-ac44-32f73dc19e35', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', '210000:HGjx1i6eSOUl3nYceyKQHQ==:vlMTvLSg+QPS82pIw8Esloz2sjhlCZd8f92N0JUPv6E=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('e7f6badd-4f33-4f02-90d5-a3338441f4d8', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', '210000:X9tpN5SQnHCv6QCCjmppew==:fC/ffogX//ifW1NbP88hoxDKc96kmvoIQ7Eksh0tGeo=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('2086c392-8a26-4391-9b97-c2b6c8988558', 'eb54e77c-2681-44c6-9801-a5abeafc5963', '210000:rc1HFyNLeBhiFr+zvCaz1w==:f8AWgLbQ/s6l0xqRUBoB+GkpUnHp0vISHmFASa+aa1A=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('fcabc02d-f4a7-446b-ab1f-ce1840883e25', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', '210000:1kkPmzJ3FeBqlzHsvDIpAw==:QU5A891SIwLHHYN+PLfnU48aRZAscbBLf8XBGkj9BOw=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('2d20fdb1-9bc0-4d9f-bd26-14bdbb686dcd', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', '210000:iX8stAqM9w+poEYbhtJOtQ==:C0xLlNz6kcmoBqSVeiBXfpfD1lwotLpLoI08iDWKfW4=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('a16f7d8a-5999-4c78-82d7-262abd0532fb', '19ff340f-4ed9-433b-ac43-8564d9b42aa8', '210000:g7kEyq2MyuT9qi4f5taGcA==:z7dBBq2IK35oACRBlAlgCDSPuD4T2TONiJbx3F6mNjk=', 'ADMINISTRADOR', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('fba6a2ac-15f5-431f-b079-51bca997a2a1', '4f32b0c6-61f7-4ef2-97a7-d5ca2a42759a', '210000:z6Ey07KEIc9JBvpcxNkSPA==:dVgodoGhMM1ZRE4xjsyL8hsZmhTu+33FN+7R2BEa8Nc=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('da9ca2af-c12c-4e04-9bc2-43da18a9626f', 'ee91db5f-efde-406a-954c-17abfedc3546', '210000:041p3m21m4n/3Goglm+8kw==:EHmM2p3b6egdU4vTquAHDN8uWasYAceJRynGHUq68rk=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('00edd6f2-7cdf-4ccc-927e-b17b4811db55', 'db65425b-c19a-4b1d-b4d1-1717293c6126', '210000:1mvO3DhzxFYAfqlrCwyRjA==:hYN5dsYHGH4jD3vc+YT4153InkhO8MV4oV/oUG8zkRQ=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('70348c6d-f177-4f94-be1e-325e4663fee9', 'c21e18c8-e670-49bb-8f32-e327ccfb3d79', '210000:LyKiYquU6f4IbD3p4adEMQ==:A+m2QcGSE8QmN60BxcN3tKiNXcmtjXb1FK6skhR4560=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('5fcb9eda-2304-4877-95a0-035efe60643b', '0e7c353e-01f0-4086-a3e1-369eb6a5e862', '210000:me2rOgqFZuxoFm93Y4afPg==:bz9dbG009nocSCzbagrzUseiFdhk16dO3yhfeftNeIU=', 'EMPLEADO', 't');
INSERT INTO cuentas_usuario (id_usuario, id_empleado, contrasena_hash, rol, activo) VALUES ('c570ed22-daba-49a8-8dac-0f43c67b14a6', '572d5abd-6c95-4c70-a317-77eb7d76d44f', '210000:oHezPZypDt7misTi7knxRQ==:vpPb4YRzBFoeK/TqSE3eZc4+RCHTtZlPOX1S7u7bt+4=', 'EMPLEADO', 't');

-- departamentos
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('3ed03317-81b2-401d-8bff-8fca062bff86', 'Cartera y Cobro', 'Departamento encargado de pagos, saldos y estados de cuenta.', 'Edificio administrativo', NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('04f009ce-cf60-4056-a025-1f47569d824f', 'Registro Académico', 'Departamento encargado de matrículas, notas y documentos académicos.', 'Edificio principal', NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('5e2f8cb9-155d-4914-844b-26ef4be83b91', 'Bienestar Estudiantil', 'Departamento encargado de orientación y apoyo estudiantil.', 'Edificio administrativo', NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('7ace173e-5bc0-40e4-9272-06d2d7772908', 'Facultad de Medicina', 'Formación médica y ciencias de la salud.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('52247fda-5d2a-4105-add3-96327dd1a571', 'Facultad de Odontología', 'Formación en odontología con apoyo de clínicas odontológicas.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('590c9b7f-d408-45fc-b2ab-45d1d41b8740', 'Facultad de Ciencias Administrativas y Económicas', 'Administración de Empresas, Contabilidad y Finanzas, Negocios Internacionales y Economía.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('9c785284-727b-40a6-a13f-30aea854461d', 'Facultad de Marketing, Diseño y Ciencias de la Comunicación', 'Marketing y Publicidad, Diseño y Comunicación Visual, y Comunicación y Relaciones Públicas.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('734d0915-b988-4eb4-aae0-c743c4a0e4e8', 'Facultad de Ingeniería y Arquitectura', 'Arquitectura, Ingeniería Civil, Ingeniería Industrial e Ingeniería en Sistemas de Información.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('59724e7c-9bf8-4332-b026-39a6e60c01f6', 'Facultad de Ciencias Jurídicas, Humanidades y Relaciones Internacionales', 'Derecho, y Diplomacia y Relaciones Internacionales.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('c08b10fe-39f1-4936-8445-879b47d03871', 'UAM College', 'Programas impartidos en inglés bajo un modelo basado en créditos.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('88b62d79-3152-4d33-ae97-d04e48e89f1e', 'Mantenimiento', 'Mantenimiento general de las instalaciones de la institución.', NULL, NULL, NULL, 't');
INSERT INTO departamentos (id_departamento, nombre, descripcion, ubicacion, correo_contacto, telefono, activo) VALUES ('64f8e8aa-f809-4e9f-997d-7cbed4f68711', 'Seguridad', 'Seguridad institucional.', NULL, NULL, NULL, 't');

-- empleados
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'Administrador General', 'ADMIN-001', 'admin@institucion.edu', 'f2b45fee-c22b-4b0b-ba43-da49e1457471', NULL, 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('d64fd1c4-4289-470c-9a3e-d007ef459ba2', 'María José López Castillo', 'EMP-001', 'maria.lopez@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '3ed03317-81b2-401d-8bff-8fca062bff86', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', 'Carlos Andrés Martínez Ruiz', 'EMP-002', 'carlos.martinez@institucion.edu', 'f2b45fee-c22b-4b0b-ba43-da49e1457471', '3ed03317-81b2-401d-8bff-8fca062bff86', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('a6dbc716-d7f6-4379-9dba-65ed40a0049a', 'Ana Gabriela Pérez Molina', 'EMP-003', 'ana.perez@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '3ed03317-81b2-401d-8bff-8fca062bff86', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', 'José Luis Hernández Soto', 'EMP-004', 'jose.hernandez@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '04f009ce-cf60-4056-a025-1f47569d824f', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('c93c58af-791f-40d6-b5b5-9651707c8a3b', 'Miguel Ángel Torres Vega', 'EMP-006', 'miguel.torres@institucion.edu', 'f2b45fee-c22b-4b0b-ba43-da49e1457471', '04f009ce-cf60-4056-a025-1f47569d824f', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('eb54e77c-2681-44c6-9801-a5abeafc5963', 'Sofía Alejandra Ramírez Díaz', 'EMP-007', 'sofia.ramirez@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('b4848d86-211e-4cc7-9a44-06f1c9c6edec', 'Valeria Isabel Cruz Navarro', 'EMP-009', 'valeria.cruz@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('de529d1e-0a4f-4a9d-b785-a615fb02c05a', 'Laura Patricia Gómez Rivas', 'EMP-005', 'laura.gomez@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '04f009ce-cf60-4056-a025-1f47569d824f', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('28617b1c-27ea-42d5-b533-6a8eb8bfafea', 'Daniel Eduardo Flores Mejía', 'EMP-008', 'daniel.flores@institucion.edu', '7c2df2d4-d6bd-46c4-9692-8eef1dc51382', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('ee91db5f-efde-406a-954c-17abfedc3546', 'Roberto Carlos Mendoza Aguilar', 'EMP-010', 'roberto.mendoza@institucion.edu', '1169deac-593a-4963-972e-20e07e755faf', '734d0915-b988-4eb4-aae0-c743c4a0e4e8', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('db65425b-c19a-4b1d-b4d1-1717293c6126', 'Karla Vanessa Ortiz Blandón', 'EMP-011', 'karla.ortiz@institucion.edu', '1169deac-593a-4963-972e-20e07e755faf', '734d0915-b988-4eb4-aae0-c743c4a0e4e8', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('c21e18c8-e670-49bb-8f32-e327ccfb3d79', 'Fernando José Castillo Reyes', 'EMP-012', 'fernando.castillo@institucion.edu', '1169deac-593a-4963-972e-20e07e755faf', '7ace173e-5bc0-40e4-9272-06d2d7772908', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('0e7c353e-01f0-4086-a3e1-369eb6a5e862', 'Pedro Antonio Rivas López', 'EMP-013', 'pedro.rivas@institucion.edu', 'b372661e-f447-4a39-8d4e-c06f02ff5e70', '88b62d79-3152-4d33-ae97-d04e48e89f1e', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('572d5abd-6c95-4c70-a317-77eb7d76d44f', 'Juan Carlos Morales Pineda', 'EMP-014', 'juan.morales@institucion.edu', 'cda96a4c-2542-4250-bcc8-247f5767cd35', '64f8e8aa-f809-4e9f-997d-7cbed4f68711', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('19ff340f-4ed9-433b-ac43-8564d9b42aa8', 'Oscar Isaac Durán Guadrón', '25011652', 'oiduran@uamv.edu.ni', 'cda96a4c-2542-4250-bcc8-247f5767cd35', '64f8e8aa-f809-4e9f-997d-7cbed4f68711', 't');
INSERT INTO empleados (id_empleado, nombre_completo, cif, correo_institucional, id_categoria, id_departamento, activo) VALUES ('4f32b0c6-61f7-4ef2-97a7-d5ca2a42759a', 'Darell Zamid Blandon', '25012652', 'dblandon@uamv.edu.ni', 'b372661e-f447-4a39-8d4e-c06f02ff5e70', '88b62d79-3152-4d33-ae97-d04e48e89f1e', 't');

-- evaluaciones
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('bb6eba8f-697e-44b3-a0aa-8236de11a1a8', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '3ed03317-81b2-401d-8bff-8fca062bff86', NULL, 'DEPARTAMENTAL', NULL, '88.00', '88.00', 'Evaluación departamental del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('28a83a4b-52c0-4a8c-a7e5-4d4b4c7b0e34', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '04f009ce-cf60-4056-a025-1f47569d824f', NULL, 'DEPARTAMENTAL', NULL, '92.00', '92.00', 'Evaluación departamental del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('cdba6e03-d347-45bb-9e53-01cdae4300f7', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '5e2f8cb9-155d-4914-844b-26ef4be83b91', NULL, 'DEPARTAMENTAL', NULL, '80.00', '80.00', 'Evaluación departamental del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('d2b7f548-ce5d-476b-bb45-bf1ab2c0fe1f', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '3ed03317-81b2-401d-8bff-8fca062bff86', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', 'INDIVIDUAL', '90.00', '88.00', '89.00', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('de4b9507-258c-4795-b6be-cdd12ccc920d', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '3ed03317-81b2-401d-8bff-8fca062bff86', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', 'INDIVIDUAL', '85.00', '88.00', '86.50', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('f040c578-16b1-4125-86fe-9270efae2d77', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '3ed03317-81b2-401d-8bff-8fca062bff86', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', 'INDIVIDUAL', '80.00', '88.00', '84.00', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('1734d194-312c-4ff0-a684-d7f51d79c319', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '04f009ce-cf60-4056-a025-1f47569d824f', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', 'INDIVIDUAL', '95.00', '92.00', '93.50', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('aba72abe-6de9-448c-bd1d-3c10b0a26206', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '04f009ce-cf60-4056-a025-1f47569d824f', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', 'INDIVIDUAL', '88.00', '92.00', '90.00', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('d98fab7e-8073-4309-bb34-4a9019cdb655', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '04f009ce-cf60-4056-a025-1f47569d824f', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', 'INDIVIDUAL', '75.00', '92.00', '83.50', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('67af29d0-f5f5-4840-9253-862d0d7db956', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 'eb54e77c-2681-44c6-9801-a5abeafc5963', 'INDIVIDUAL', '85.00', '80.00', '82.50', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('5e7a4c2e-e01a-4dc2-a48f-ec9f7d4cea7a', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '5e2f8cb9-155d-4914-844b-26ef4be83b91', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', 'INDIVIDUAL', '78.00', '80.00', '79.00', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');
INSERT INTO evaluaciones (id_evaluacion, id_periodo, id_departamento, id_empleado, tipo, puntuacion_individual, puntuacion_departamental, puntuacion_final, observaciones, fecha_evaluacion, evaluado_por, estado) VALUES ('d52142cc-31b3-4047-bf5f-a9b1102150ff', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', 'INDIVIDUAL', '82.00', '80.00', '81.00', 'Evaluación individual del semestre.', '2026-07-05', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', 'FINALIZADA');

-- objetivos
INSERT INTO objetivos (id_objetivo, id_objetivo_padre, nombre, descripcion, nivel, indicador, meta, unidad_medida, activo) VALUES ('2a5fec74-dd55-4088-96ef-e79122e21858', NULL, 'Mejorar la atención al estudiante', 'Brindar una atención rápida y de calidad a los estudiantes.', 'GENERAL', NULL, NULL, NULL, 't');
INSERT INTO objetivos (id_objetivo, id_objetivo_padre, nombre, descripcion, nivel, indicador, meta, unidad_medida, activo) VALUES ('6660d043-ef57-4b1b-b234-afcb76152f10', NULL, 'Optimizar los procesos administrativos', 'Hacer más eficientes los procesos internos de la institución.', 'GENERAL', NULL, NULL, NULL, 't');
INSERT INTO objetivos (id_objetivo, id_objetivo_padre, nombre, descripcion, nivel, indicador, meta, unidad_medida, activo) VALUES ('26cd31c2-f8f1-405c-bf8e-d14e76ae79a0', '2a5fec74-dd55-4088-96ef-e79122e21858', 'Reducir el tiempo de respuesta de solicitudes', 'Atender las solicitudes estudiantiles en menos tiempo.', 'ESPECIFICO', 'Horas promedio de respuesta', '48', 'horas', 't');
INSERT INTO objetivos (id_objetivo, id_objetivo_padre, nombre, descripcion, nivel, indicador, meta, unidad_medida, activo) VALUES ('70f236bc-2269-4285-86b3-33ad5c87668d', '2a5fec74-dd55-4088-96ef-e79122e21858', 'Aumentar la satisfacción en encuestas de servicio', 'Mejorar la percepción del servicio en las encuestas.', 'ESPECIFICO', 'Satisfacción promedio', '85', '%', 't');
INSERT INTO objetivos (id_objetivo, id_objetivo_padre, nombre, descripcion, nivel, indicador, meta, unidad_medida, activo) VALUES ('1f6adf71-af4a-47b0-ab0f-2d14daadb35d', '6660d043-ef57-4b1b-b234-afcb76152f10', 'Digitalizar los expedientes académicos', 'Contar con los expedientes académicos en formato digital.', 'ESPECIFICO', 'Expedientes digitalizados', '100', '%', 't');
INSERT INTO objetivos (id_objetivo, id_objetivo_padre, nombre, descripcion, nivel, indicador, meta, unidad_medida, activo) VALUES ('71751d1b-158b-41dc-9ad0-9c8f42e6ef6e', '6660d043-ef57-4b1b-b234-afcb76152f10', 'Mantener al día los estados de cuenta', 'Mantener actualizados los saldos de los estudiantes.', 'ESPECIFICO', 'Cuentas al día', '95', '%', 't');

-- objetivos_departamento
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('840f2128-2ebc-434e-ac82-ca3bcf8010b0', '71751d1b-158b-41dc-9ad0-9c8f42e6ef6e', '3ed03317-81b2-401d-8bff-8fca062bff86', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '2026-01-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('36c2a591-0011-414d-a43c-05bb958ac4fb', '1f6adf71-af4a-47b0-ab0f-2d14daadb35d', '04f009ce-cf60-4056-a025-1f47569d824f', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '2026-01-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('bd282e98-d7ed-4b5f-9b73-af6682c16f85', '70f236bc-2269-4285-86b3-33ad5c87668d', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 'd534f9a5-061c-462b-a59b-69afc8d99cc1', '2026-01-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('917f2f01-1713-4338-bf63-cee877f67dce', '71751d1b-158b-41dc-9ad0-9c8f42e6ef6e', '3ed03317-81b2-401d-8bff-8fca062bff86', 'eb6d31b9-c0cc-4bea-99bf-404cec5805f2', '2026-07-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('ab75f287-198f-4cc1-b736-fe9ac375a103', '26cd31c2-f8f1-405c-bf8e-d14e76ae79a0', '3ed03317-81b2-401d-8bff-8fca062bff86', 'eb6d31b9-c0cc-4bea-99bf-404cec5805f2', '2026-07-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('443a2e74-d235-481a-8b5b-ce27a518eddd', '1f6adf71-af4a-47b0-ab0f-2d14daadb35d', '04f009ce-cf60-4056-a025-1f47569d824f', 'eb6d31b9-c0cc-4bea-99bf-404cec5805f2', '2026-07-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('4e23505a-8623-4be2-8c60-1e1144787648', '26cd31c2-f8f1-405c-bf8e-d14e76ae79a0', '04f009ce-cf60-4056-a025-1f47569d824f', 'eb6d31b9-c0cc-4bea-99bf-404cec5805f2', '2026-07-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('30cf0b32-c897-4e1b-888d-53ca0906b77b', '70f236bc-2269-4285-86b3-33ad5c87668d', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 'eb6d31b9-c0cc-4bea-99bf-404cec5805f2', '2026-07-20');
INSERT INTO objetivos_departamento (id_asignacion, id_objetivo, id_departamento, id_periodo, fecha_asignacion) VALUES ('74cd3fd8-35d8-46bd-9f1e-f94143890263', '26cd31c2-f8f1-405c-bf8e-d14e76ae79a0', '5e2f8cb9-155d-4914-844b-26ef4be83b91', 'eb6d31b9-c0cc-4bea-99bf-404cec5805f2', '2026-07-20');

-- periodos_evaluacion
INSERT INTO periodos_evaluacion (id_periodo, nombre, descripcion, fecha_inicio, fecha_fin, estado) VALUES ('d534f9a5-061c-462b-a59b-69afc8d99cc1', 'Primer semestre 2026', 'Evaluación del desempeño de enero a junio de 2026.', '2026-01-15', '2026-06-30', 'CERRADO');
INSERT INTO periodos_evaluacion (id_periodo, nombre, descripcion, fecha_inicio, fecha_fin, estado) VALUES ('eb6d31b9-c0cc-4bea-99bf-404cec5805f2', 'Segundo semestre 2026', 'Evaluación del desempeño de julio a diciembre de 2026.', '2026-07-15', '2026-12-15', 'EN_CURSO');
INSERT INTO periodos_evaluacion (id_periodo, nombre, descripcion, fecha_inicio, fecha_fin, estado) VALUES ('8cdb99b2-393e-4a91-83d5-213656cda37b', 'Primer semestre 2027', 'Evaluación del desempeño de enero a junio de 2027.', '2027-01-15', '2027-06-30', 'PLANIFICADO');

-- responsables_objetivos
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('9050d8ae-5730-495d-b3ba-268d9509dfa5', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('4e41375f-3756-4859-a24b-6f0c56dae57d', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('28d45846-70b1-463a-87ec-fda8862bf4e0', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('3c58d9a2-a5a3-4610-9da8-939076661920', '36c2a591-0011-414d-a43c-05bb958ac4fb', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('c5ae9acf-eca2-4ce7-a166-2ee821ad44e5', '36c2a591-0011-414d-a43c-05bb958ac4fb', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('436c8ff8-c109-4728-9bad-fa25e4394320', '36c2a591-0011-414d-a43c-05bb958ac4fb', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('a75b1916-fc47-4d31-a421-503bab037c2f', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('ab8a84e3-2595-45b1-8c88-bcffc59d3ae0', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('204f288c-7ff9-4c4e-b9ab-97dd1f234c7c', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', 'eb54e77c-2681-44c6-9801-a5abeafc5963', '2026-01-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('1dad8b52-1fa1-4bb4-8237-d40e4e37763b', '917f2f01-1713-4338-bf63-cee877f67dce', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('e1b24ddb-f5c2-4d23-92e9-2796d3cc29ad', '917f2f01-1713-4338-bf63-cee877f67dce', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('c92ba541-5d8f-46aa-9666-22d1a73ec1b3', '917f2f01-1713-4338-bf63-cee877f67dce', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('44c3d3c2-85be-441e-a315-036fcd96b851', 'ab75f287-198f-4cc1-b736-fe9ac375a103', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('da2a14b3-13db-41af-b01b-820de93b57fe', 'ab75f287-198f-4cc1-b736-fe9ac375a103', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('be0be2e8-f632-4c86-8c67-c68dc4580848', 'ab75f287-198f-4cc1-b736-fe9ac375a103', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('f398f232-82a1-4033-8d9b-7adab7b5da0f', '443a2e74-d235-481a-8b5b-ce27a518eddd', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('49956b3b-c995-481e-8518-03542f9c1aac', '443a2e74-d235-481a-8b5b-ce27a518eddd', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('6286f94c-a6d5-46d6-8989-a62b5467cfa7', '443a2e74-d235-481a-8b5b-ce27a518eddd', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('0f2e0abb-e8d5-45eb-b431-717993627a72', '4e23505a-8623-4be2-8c60-1e1144787648', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('b91a519b-3bb8-47ac-b8cd-b167447666ff', '4e23505a-8623-4be2-8c60-1e1144787648', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('e52ab7ec-3a90-4989-9f03-a638e0d4f2e0', '4e23505a-8623-4be2-8c60-1e1144787648', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('f4d14b26-3e7e-472c-95e6-35b3a3fbfa17', '30cf0b32-c897-4e1b-888d-53ca0906b77b', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('936933a2-77d4-424c-8f59-118a18bc1707', '30cf0b32-c897-4e1b-888d-53ca0906b77b', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('b8545b2a-349c-4ca3-b207-3b0041c8e3f0', '30cf0b32-c897-4e1b-888d-53ca0906b77b', 'eb54e77c-2681-44c6-9801-a5abeafc5963', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('da2e0d83-e8da-4956-9da2-1e527cdebbee', '74cd3fd8-35d8-46bd-9f1e-f94143890263', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('2d3f31a7-f4d7-4eca-8f15-4d5153f46e74', '74cd3fd8-35d8-46bd-9f1e-f94143890263', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', '2026-07-20');
INSERT INTO responsables_objetivos (id_responsabilidad, id_asignacion, id_empleado, fecha_asignacion) VALUES ('5ad678ed-31d8-4621-a467-928cfdee313d', '74cd3fd8-35d8-46bd-9f1e-f94143890263', 'eb54e77c-2681-44c6-9801-a5abeafc5963', '2026-07-20');

-- seguimientos
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('bb651f17-3ba8-4e64-80d1-1b91b26403b3', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', NULL, '3ed03317-81b2-401d-8bff-8fca062bff86', '2026-03-31', 'DEPARTAMENTAL', '45', 'Primer corte del semestre.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('0f3ebf83-7796-4470-9e5b-83cc66d38bd2', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', NULL, '3ed03317-81b2-401d-8bff-8fca062bff86', '2026-06-25', 'DEPARTAMENTAL', '88', 'Cierre del semestre.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('0578214f-8832-4fbe-8777-97500206de24', '36c2a591-0011-414d-a43c-05bb958ac4fb', NULL, '04f009ce-cf60-4056-a025-1f47569d824f', '2026-03-31', 'DEPARTAMENTAL', '40', 'Primer corte del semestre.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('6234ad94-d49c-4a84-9473-b6ae3f5e3d18', '36c2a591-0011-414d-a43c-05bb958ac4fb', NULL, '04f009ce-cf60-4056-a025-1f47569d824f', '2026-06-25', 'DEPARTAMENTAL', '92', 'Cierre del semestre.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('25f3d17d-d3b4-47aa-9e42-2f651131dd3b', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', NULL, '5e2f8cb9-155d-4914-844b-26ef4be83b91', '2026-03-31', 'DEPARTAMENTAL', '50', 'Primer corte del semestre.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('aea98dbd-b69c-4484-8e03-f6499a15934f', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', NULL, '5e2f8cb9-155d-4914-844b-26ef4be83b91', '2026-06-25', 'DEPARTAMENTAL', '80', 'Cierre del semestre.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('d9059a0c-9e80-443a-9383-5379b0c8bb5f', '917f2f01-1713-4338-bf63-cee877f67dce', NULL, '3ed03317-81b2-401d-8bff-8fca062bff86', '2026-09-30', 'DEPARTAMENTAL', '35', 'Avance de septiembre.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('2e7cacc9-54a9-4202-94f8-63595fca4b90', '917f2f01-1713-4338-bf63-cee877f67dce', NULL, '3ed03317-81b2-401d-8bff-8fca062bff86', '2026-11-10', 'DEPARTAMENTAL', '62', 'Avance de noviembre.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('8a72e7dd-3001-436b-aa72-74ada5a9a37c', 'ab75f287-198f-4cc1-b736-fe9ac375a103', NULL, '3ed03317-81b2-401d-8bff-8fca062bff86', '2026-10-15', 'DEPARTAMENTAL', '50', 'Avance de octubre.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('10e4879c-a12a-4ab5-9402-747647fb7eb9', '443a2e74-d235-481a-8b5b-ce27a518eddd', NULL, '04f009ce-cf60-4056-a025-1f47569d824f', '2026-09-30', 'DEPARTAMENTAL', '30', 'Avance de septiembre.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('3903a0ba-c464-4db0-9412-1173ea41fe38', '443a2e74-d235-481a-8b5b-ce27a518eddd', NULL, '04f009ce-cf60-4056-a025-1f47569d824f', '2026-11-10', 'DEPARTAMENTAL', '58', 'Avance de noviembre.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('906b770a-e737-4fa3-8ae8-228dc6e0edac', '4e23505a-8623-4be2-8c60-1e1144787648', NULL, '04f009ce-cf60-4056-a025-1f47569d824f', '2026-10-15', 'DEPARTAMENTAL', '45', 'Avance de octubre.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('1b4077f5-786a-42f1-ad9e-9f551ca8affb', '30cf0b32-c897-4e1b-888d-53ca0906b77b', NULL, '5e2f8cb9-155d-4914-844b-26ef4be83b91', '2026-09-30', 'DEPARTAMENTAL', '40', 'Avance de septiembre.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('79672f25-e14b-40d6-965c-85793092fac9', '30cf0b32-c897-4e1b-888d-53ca0906b77b', NULL, '5e2f8cb9-155d-4914-844b-26ef4be83b91', '2026-11-10', 'DEPARTAMENTAL', '70', 'Avance de noviembre.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('90f7f1a4-9cfb-453a-836d-7be40260775d', '74cd3fd8-35d8-46bd-9f1e-f94143890263', NULL, '5e2f8cb9-155d-4914-844b-26ef4be83b91', '2026-10-15', 'DEPARTAMENTAL', '55', 'Avance de octubre.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('2355d2ef-0186-41e1-bbda-54fe42d97de0', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', NULL, '2026-06-20', 'INDIVIDUAL', '90', 'Avance individual.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('00756338-cf9d-4c9f-8106-d8526c48b43e', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', NULL, '2026-06-20', 'INDIVIDUAL', '85', 'Avance individual.', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('7abb6f9a-cdcd-47ab-9bc4-dcdfe9b2e40e', '840f2128-2ebc-434e-ac82-ca3bcf8010b0', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', NULL, '2026-06-20', 'INDIVIDUAL', '80', 'Avance individual.', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('e1496135-16f8-4cfe-ab92-fe298a8b8072', '36c2a591-0011-414d-a43c-05bb958ac4fb', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', NULL, '2026-06-20', 'INDIVIDUAL', '95', 'Avance individual.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('3f2bf2a3-6dd4-4573-8d39-4ce425715304', '36c2a591-0011-414d-a43c-05bb958ac4fb', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', NULL, '2026-06-20', 'INDIVIDUAL', '88', 'Avance individual.', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('940a33ba-e5e0-480b-ac73-118f80609ee2', '36c2a591-0011-414d-a43c-05bb958ac4fb', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', NULL, '2026-06-20', 'INDIVIDUAL', '75', 'Avance individual.', 'c93c58af-791f-40d6-b5b5-9651707c8a3b');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('6c84a483-5c9c-409e-8484-30f2a11ad074', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', 'eb54e77c-2681-44c6-9801-a5abeafc5963', NULL, '2026-06-20', 'INDIVIDUAL', '85', 'Avance individual.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('5f41fd9c-b3bb-4e2c-b392-7cd3f4c06e8d', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', NULL, '2026-06-20', 'INDIVIDUAL', '78', 'Avance individual.', '28617b1c-27ea-42d5-b533-6a8eb8bfafea');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('39122a9b-2f0d-41c6-9b24-bbc50fd8a05d', 'bd282e98-d7ed-4b5f-9b73-af6682c16f85', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', NULL, '2026-06-20', 'INDIVIDUAL', '82', 'Avance individual.', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('a7858d1f-7d04-4669-996c-2986f3156192', '917f2f01-1713-4338-bf63-cee877f67dce', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', NULL, '2026-11-05', 'INDIVIDUAL', '70', 'Avance individual.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('14d045ad-100b-48b6-9df6-b0fb29638ed6', 'ab75f287-198f-4cc1-b736-fe9ac375a103', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', NULL, '2026-11-05', 'INDIVIDUAL', '60', 'Avance individual.', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('f5c221d7-37cb-4c2d-865e-de203d4295cf', '917f2f01-1713-4338-bf63-cee877f67dce', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', NULL, '2026-11-05', 'INDIVIDUAL', '55', 'Avance individual.', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('293b4b07-e327-4f54-83b2-7db9f5ae0200', 'ab75f287-198f-4cc1-b736-fe9ac375a103', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', NULL, '2026-11-05', 'INDIVIDUAL', '40', 'Avance individual.', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('0be216b1-0fee-4771-98fb-9936b5cd3bb6', '917f2f01-1713-4338-bf63-cee877f67dce', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', NULL, '2026-11-05', 'INDIVIDUAL', '65', 'Avance individual.', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('4cd0722e-264d-4789-bda0-0d79082d26b6', '443a2e74-d235-481a-8b5b-ce27a518eddd', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', NULL, '2026-11-05', 'INDIVIDUAL', '68', 'Avance individual.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('6875e6a6-ff2e-45c2-9e23-a7bd5ee92b59', '4e23505a-8623-4be2-8c60-1e1144787648', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', NULL, '2026-11-05', 'INDIVIDUAL', '50', 'Avance individual.', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('2a275496-c298-4080-b6f7-9915638866cb', '443a2e74-d235-481a-8b5b-ce27a518eddd', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', NULL, '2026-11-05', 'INDIVIDUAL', '60', 'Avance individual.', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('8d537327-15cf-481c-9a0c-ad3474edd826', '4e23505a-8623-4be2-8c60-1e1144787648', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', NULL, '2026-11-05', 'INDIVIDUAL', '35', 'Avance individual.', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('b269a7c0-aae2-434c-be82-9f3c478f63c6', '443a2e74-d235-481a-8b5b-ce27a518eddd', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', NULL, '2026-11-05', 'INDIVIDUAL', '52', 'Avance individual.', 'c93c58af-791f-40d6-b5b5-9651707c8a3b');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('5fbe3dc0-cced-4b3b-be4e-c4a111173634', '30cf0b32-c897-4e1b-888d-53ca0906b77b', 'eb54e77c-2681-44c6-9801-a5abeafc5963', NULL, '2026-11-05', 'INDIVIDUAL', '75', 'Avance individual.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('cbad4512-039a-4367-85a1-81a7321982a4', '74cd3fd8-35d8-46bd-9f1e-f94143890263', 'eb54e77c-2681-44c6-9801-a5abeafc5963', NULL, '2026-11-05', 'INDIVIDUAL', '60', 'Avance individual.', 'eb54e77c-2681-44c6-9801-a5abeafc5963');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('cb5f5654-5269-43cb-8b04-d94e5eee969f', '30cf0b32-c897-4e1b-888d-53ca0906b77b', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', NULL, '2026-11-05', 'INDIVIDUAL', '66', 'Avance individual.', '28617b1c-27ea-42d5-b533-6a8eb8bfafea');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('3102dc87-bfdf-4dc6-a284-edabe3ab55c2', '74cd3fd8-35d8-46bd-9f1e-f94143890263', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', NULL, '2026-11-05', 'INDIVIDUAL', '48', 'Avance individual.', '28617b1c-27ea-42d5-b533-6a8eb8bfafea');
INSERT INTO seguimientos (id_seguimiento, id_asignacion, id_empleado, id_departamento, fecha_seguimiento, tipo, porcentaje_avance, observaciones, registrado_por) VALUES ('425171f7-1fa2-4857-8e67-b6371325b7ec', '30cf0b32-c897-4e1b-888d-53ca0906b77b', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', NULL, '2026-11-05', 'INDIVIDUAL', '72', 'Avance individual.', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec');

-- supervisiones
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('3aa4e32a-cf05-4efa-9f0f-883d0754d40b', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('63ada01f-7373-4bd3-94c2-628d929b5d5a', '2212a5a0-4c17-4b34-8dbe-c8b9549e2b9d', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('37dde843-dc68-4b71-9103-7a2d4e8ce4b6', 'a6dbc716-d7f6-4379-9dba-65ed40a0049a', 'd64fd1c4-4289-470c-9a3e-d007ef459ba2', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('8d2c2fa0-a885-4eb9-bd78-005493fbd71a', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('7330ff45-0953-4340-b621-797ee769b6ca', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('cf7e873a-6b15-42ab-a665-f1f745e9a729', 'eb54e77c-2681-44c6-9801-a5abeafc5963', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('04e90fd9-f227-44b7-9c0a-8f7cfd0f9e1c', '28617b1c-27ea-42d5-b533-6a8eb8bfafea', 'eb54e77c-2681-44c6-9801-a5abeafc5963', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('5f30bc77-c040-41fd-a2a2-908172f6b2da', 'b4848d86-211e-4cc7-9a44-06f1c9c6edec', 'eb54e77c-2681-44c6-9801-a5abeafc5963', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('0e60e874-5a0a-491d-b872-28b68ca4f7cc', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', 'de529d1e-0a4f-4a9d-b785-a615fb02c05a', '2026-01-10', '2026-07-01', 'f');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('409dd4dc-f617-4b45-bcdc-b6dcd76c1523', 'c93c58af-791f-40d6-b5b5-9651707c8a3b', '92a0cab5-8ab0-46d2-8c7a-c5e4ff2c4400', '2026-07-01', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('e9c992f1-32b0-47c3-9d7a-52e4beb55521', 'ee91db5f-efde-406a-954c-17abfedc3546', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('3f60dd55-6183-4f3a-b38d-ba26bd0798f8', 'db65425b-c19a-4b1d-b4d1-1717293c6126', 'ee91db5f-efde-406a-954c-17abfedc3546', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('e4c9015f-cf8c-413b-9b3f-14d4e2dfab63', 'c21e18c8-e670-49bb-8f32-e327ccfb3d79', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('d290421c-5e14-4bad-b067-52a3d9a10eb5', '0e7c353e-01f0-4086-a3e1-369eb6a5e862', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');
INSERT INTO supervisiones (id_supervision, id_empleado, id_jefe, fecha_inicio, fecha_fin, activo) VALUES ('3c01e7ab-0de8-41e6-a96f-fd4c435dd555', '572d5abd-6c95-4c70-a317-77eb7d76d44f', 'a6ed65c9-8f0d-41b4-a0d6-b672dbdb36d9', '2026-01-10', NULL, 't');

-- ===================== LLAVES FORÁNEAS =====================
ALTER TABLE administradores_sistema ADD CONSTRAINT fk_administrador_usuario FOREIGN KEY (id_usuario) REFERENCES cuentas_usuario(id_usuario);
ALTER TABLE categoria_departamento ADD CONSTRAINT categoria_departamento_id_categoria_fkey FOREIGN KEY (id_categoria) REFERENCES categorias_personal(id_categoria);
ALTER TABLE categoria_departamento ADD CONSTRAINT categoria_departamento_id_departamento_fkey FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento);
ALTER TABLE cuentas_usuario ADD CONSTRAINT fk_cuentas_empleados FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado);
ALTER TABLE empleados ADD CONSTRAINT fk_empleados_categoria FOREIGN KEY (id_categoria) REFERENCES categorias_personal(id_categoria);
ALTER TABLE empleados ADD CONSTRAINT fk_empleados_departamento FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento);
ALTER TABLE evaluaciones ADD CONSTRAINT fk_evaluacion_departamento FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento);
ALTER TABLE evaluaciones ADD CONSTRAINT fk_evaluacion_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado);
ALTER TABLE evaluaciones ADD CONSTRAINT fk_evaluacion_evaluador FOREIGN KEY (evaluado_por) REFERENCES empleados(id_empleado);
ALTER TABLE evaluaciones ADD CONSTRAINT fk_evaluacion_periodo FOREIGN KEY (id_periodo) REFERENCES periodos_evaluacion(id_periodo);
ALTER TABLE objetivos ADD CONSTRAINT fk_objetivo_padre FOREIGN KEY (id_objetivo_padre) REFERENCES objetivos(id_objetivo);
ALTER TABLE objetivos_departamento ADD CONSTRAINT fk_asignacion_departamento FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento);
ALTER TABLE objetivos_departamento ADD CONSTRAINT fk_asignacion_periodo FOREIGN KEY (id_periodo) REFERENCES periodos_evaluacion(id_periodo);
ALTER TABLE objetivos_departamento ADD CONSTRAINT fk_objetivo_departamento FOREIGN KEY (id_objetivo) REFERENCES objetivos(id_objetivo);
ALTER TABLE responsables_objetivos ADD CONSTRAINT fk_responsabilidad_asignacion FOREIGN KEY (id_asignacion) REFERENCES objetivos_departamento(id_asignacion);
ALTER TABLE responsables_objetivos ADD CONSTRAINT fk_responsabilidad_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado);
ALTER TABLE seguimientos ADD CONSTRAINT fk_seguimiento_asignacion FOREIGN KEY (id_asignacion) REFERENCES objetivos_departamento(id_asignacion);
ALTER TABLE seguimientos ADD CONSTRAINT fk_seguimiento_departamento FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento);
ALTER TABLE seguimientos ADD CONSTRAINT fk_seguimiento_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado);
ALTER TABLE seguimientos ADD CONSTRAINT fk_seguimiento_registrador FOREIGN KEY (registrado_por) REFERENCES empleados(id_empleado);
ALTER TABLE supervisiones ADD CONSTRAINT fk_supervision_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado);
ALTER TABLE supervisiones ADD CONSTRAINT fk_supervision_jefe FOREIGN KEY (id_jefe) REFERENCES empleados(id_empleado);

-- ===================== ÍNDICES =====================
CREATE UNIQUE INDEX ux_supervisiones_empleado_activo ON public.supervisiones USING btree (id_empleado) WHERE (activo = true);

-- ===================== FUNCIONES Y TRIGGERS =====================
CREATE OR REPLACE FUNCTION public.validar_categoria_departamento()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
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
$function$;

CREATE TRIGGER trg_empleados_categoria_departamento BEFORE INSERT OR UPDATE OF id_categoria, id_departamento ON public.empleados FOR EACH ROW EXECUTE FUNCTION validar_categoria_departamento();

COMMIT;
