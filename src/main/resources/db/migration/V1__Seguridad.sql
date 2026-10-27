-- ===========================================================================
-- LudusERP - Modulo de seguridad y usuarios
-- ===========================================================================
-- Una sola tabla de autenticacion para todo el mundo: lo que distingue a un
-- cliente de un trabajador es el rol.
--
-- Al ser producto digital no hay envio, asi que del cliente solo se guarda
-- telefono, pais y fecha de nacimiento. El pais es el que importa: de el
-- depende el IVA que se aplica en la venta.
--
-- Los roles no son una tabla: son el enum Rol, guardado como texto en
-- usuario_rol. No hay tabla de permisos; el acceso se decide por rol.
-- ===========================================================================

CREATE TABLE usuario (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(180) NOT NULL,
    password_hash    VARCHAR(60)  NOT NULL,
    nombre           VARCHAR(80)  NOT NULL,
    apellidos        VARCHAR(120) NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta       DATETIME     NOT NULL,
    telefono         VARCHAR(20)  NULL,
    pais             VARCHAR(60)  NULL,
    fecha_nacimiento DATE         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Roles de cada usuario. Clave primaria compuesta: un mismo rol no se puede
-- repetir dos veces en el mismo usuario.
CREATE TABLE usuario_rol (
    usuario_id BIGINT      NOT NULL,
    rol        VARCHAR(30) NOT NULL,
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuario_rol_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
