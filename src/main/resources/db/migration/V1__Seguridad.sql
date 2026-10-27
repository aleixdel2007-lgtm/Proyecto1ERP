CREATE TABLE usuario (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(180) NOT NULL,
    password_hash    VARCHAR(60)  NOT NULL,
    rol              VARCHAR(30)  NOT NULL,
    nombre           VARCHAR(80)  NOT NULL,
    apellidos        VARCHAR(120) NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta       DATETIME     NOT NULL,
    telefono         VARCHAR(20)  NULL,
    direccion        VARCHAR(160) NULL,
    ciudad           VARCHAR(80)  NULL,
    pais             VARCHAR(60)  NULL,
    fecha_nacimiento DATE         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
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

CREATE TABLE empleado (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id         BIGINT      NOT NULL,
    departamento_id    BIGINT      NOT NULL,
    nif                VARCHAR(20) NULL,
    iban               VARCHAR(34) NULL,
    fecha_contratacion DATE        NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_empleado_usuario UNIQUE (usuario_id),
    CONSTRAINT uk_empleado_nif UNIQUE (nif),
    CONSTRAINT fk_empleado_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_empleado_departamento
        FOREIGN KEY (departamento_id) REFERENCES departamento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

ALTER TABLE departamento
    ADD CONSTRAINT fk_departamento_responsable
        FOREIGN KEY (responsable_id) REFERENCES empleado (id);
