-- Modulo de seguridad: usuarios, sus caras y los departamentos.
--
-- Usuario es una clase abstracta con estrategia JOINED. Esta tabla guarda lo
-- comun a todos, y cliente y trabajador guardan lo suyo compartiendo la MISMA
-- clave primaria. Por eso sus id no son AUTO_INCREMENT: son la id del usuario y
-- a la vez la clave ajena que apunta a el, y esa doble condicion es la que hace
-- que la relacion 1:1 no pueda romperse.

CREATE TABLE usuario (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    email                 VARCHAR(180) NOT NULL,
    password_hash         VARCHAR(60)  NOT NULL,
    rol                   VARCHAR(30)  NOT NULL,
    nombre                VARCHAR(80)  NOT NULL,
    apellidos             VARCHAR(120) NULL,
    activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    debe_cambiar_password BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_alta            DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE cliente (
    id               BIGINT       NOT NULL,
    telefono         VARCHAR(20)  NULL,
    direccion        VARCHAR(160) NULL,
    ciudad           VARCHAR(80)  NULL,
    pais             VARCHAR(60)  NULL,
    fecha_nacimiento DATE         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cliente_usuario
        FOREIGN KEY (id) REFERENCES usuario (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE departamento (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(80)  NOT NULL,
    descripcion    VARCHAR(255) NULL,
    responsable_id BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_departamento_nombre UNIQUE (nombre),
    CONSTRAINT uk_departamento_responsable UNIQUE (responsable_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE trabajador (
    id                 BIGINT       NOT NULL,
    departamento_id    BIGINT       NOT NULL,
    cargo              VARCHAR(80)  NULL,
    nif                VARCHAR(128) NULL,
    nif_hash           VARCHAR(64)  NULL,
    iban               VARCHAR(128) NULL,
    fecha_contratacion DATE         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_trabajador_nif_hash UNIQUE (nif_hash),
    CONSTRAINT fk_trabajador_usuario
        FOREIGN KEY (id) REFERENCES usuario (id),
    CONSTRAINT fk_trabajador_departamento
        FOREIGN KEY (departamento_id) REFERENCES departamento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

ALTER TABLE departamento
    ADD CONSTRAINT fk_departamento_responsable
        FOREIGN KEY (responsable_id) REFERENCES trabajador (id);
