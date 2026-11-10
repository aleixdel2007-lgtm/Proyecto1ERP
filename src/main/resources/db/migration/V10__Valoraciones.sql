-- Valoraciones de los clientes sobre los videojuegos. cliente_id apunta a cliente,
-- cuya clave es la id de usuario (herencia JOINED), por eso no hace falta otra FK a usuario.
-- Si el cliente esta inactivo la valoracion se considera inactiva (se resuelve en la app).

CREATE TABLE valoracion (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    cliente_id    BIGINT       NOT NULL,
    videojuego_id BIGINT       NOT NULL,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVA',
    titulo        VARCHAR(150) NOT NULL,
    descripcion   TEXT         NULL,
    valoracion    INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_valoracion_rango CHECK (valoracion BETWEEN 1 AND 5),
    CONSTRAINT fk_valoracion_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_valoracion_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
