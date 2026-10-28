CREATE TABLE solicitud_publicacion (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    numero            VARCHAR(20)   NOT NULL,
    proveedor_id      BIGINT        NOT NULL,
    estado            VARCHAR(30)   NOT NULL,
    titulo            VARCHAR(150)  NOT NULL,
    sinopsis          TEXT          NULL,
    pegi              INT           NOT NULL,
    precio_propuesto  DECIMAL(10,2) NULL,
    version           VARCHAR(20)   NOT NULL,
    nombre_archivo    VARCHAR(255)  NOT NULL,
    ruta_archivo      VARCHAR(255)  NOT NULL,
    tamano_bytes      BIGINT        NOT NULL,
    sha256            VARCHAR(64)   NULL,
    notas_estudio     TEXT          NULL,
    numero_envio      INT           NOT NULL,
    fecha_envio       DATETIME      NOT NULL,
    trabajador_id     BIGINT        NULL,
    fecha_revision    DATETIME      NULL,
    motivo            VARCHAR(500)  NULL,
    videojuego_id     BIGINT        NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_solicitud_numero UNIQUE (numero),
    CONSTRAINT uk_solicitud_videojuego UNIQUE (videojuego_id),
    CONSTRAINT fk_solicitud_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedor (id),
    CONSTRAINT fk_solicitud_trabajador
        FOREIGN KEY (trabajador_id) REFERENCES trabajador (id),
    CONSTRAINT fk_solicitud_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    INDEX idx_solicitud_cola (estado, fecha_envio),
    INDEX idx_solicitud_estudio (proveedor_id, fecha_envio)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE solicitud_categoria (
    solicitud_id BIGINT      NOT NULL,
    categoria    VARCHAR(30) NOT NULL,
    PRIMARY KEY (solicitud_id, categoria),
    CONSTRAINT fk_solicitud_categoria_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud_publicacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE solicitud_plataforma (
    solicitud_id BIGINT      NOT NULL,
    plataforma   VARCHAR(20) NOT NULL,
    PRIMARY KEY (solicitud_id, plataforma),
    CONSTRAINT fk_solicitud_plataforma_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud_publicacion (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE imagen_solicitud (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    solicitud_id      BIGINT       NOT NULL,
    ruta              VARCHAR(255) NOT NULL,
    texto_alternativo VARCHAR(160) NULL,
    tipo              VARCHAR(20)  NOT NULL,
    orden             INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_imagen_solicitud_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud_publicacion (id),
    INDEX idx_imagen_solicitud_galeria (solicitud_id, tipo, orden)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE version_juego (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    videojuego_id  BIGINT       NOT NULL,
    numero         VARCHAR(20)  NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo   VARCHAR(255) NOT NULL,
    tamano_bytes   BIGINT       NOT NULL,
    sha256         VARCHAR(64)  NULL,
    activa         BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_subida   DATETIME     NOT NULL,
    notas          TEXT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_version_juego_numero UNIQUE (videojuego_id, numero),
    CONSTRAINT fk_version_juego_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    INDEX idx_version_juego_activa (videojuego_id, activa)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
