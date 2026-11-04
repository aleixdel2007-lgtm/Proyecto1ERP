-- Imagen de perfil del usuario. Un usuario tiene como mucho una, asi que la clave
-- primaria es la propia id del usuario, igual que en cliente, empleado y colaborador.

CREATE TABLE imagen_usuario (
    id_usuario        BIGINT       NOT NULL,
    ruta_imagen       VARCHAR(255) NOT NULL,
    texto_alternativo VARCHAR(160) NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_imagen_usuario_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
