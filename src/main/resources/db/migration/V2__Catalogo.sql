CREATE TABLE videojuego (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    titulo            VARCHAR(150)  NOT NULL,
    slug              VARCHAR(160)  NOT NULL,
    sinopsis          TEXT          NULL,
    estudio           VARCHAR(120)  NULL,
    precio            DECIMAL(10,2) NOT NULL,
    precio_anterior   DECIMAL(10,2) NULL,
    fecha_lanzamiento DATE          NULL,
    pegi              INT           NOT NULL,
    activo            BOOLEAN       NOT NULL DEFAULT TRUE,
    stock_ilimitado   BOOLEAN       NOT NULL DEFAULT FALSE,
    umbral_stock_bajo INT           NOT NULL DEFAULT 10,
    PRIMARY KEY (id),
    CONSTRAINT uk_videojuego_slug UNIQUE (slug),
    INDEX idx_videojuego_activo (activo, titulo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE imagen_videojuego (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    videojuego_id     BIGINT       NOT NULL,
    ruta              VARCHAR(255) NOT NULL,
    texto_alternativo VARCHAR(160) NULL,
    tipo              VARCHAR(20)  NOT NULL,
    orden             INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_imagen_videojuego_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    INDEX idx_imagen_videojuego_galeria (videojuego_id, tipo, orden)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE videojuego_categoria (
    videojuego_id BIGINT      NOT NULL,
    categoria     VARCHAR(30) NOT NULL,
    PRIMARY KEY (videojuego_id, categoria),
    CONSTRAINT fk_videojuego_categoria_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    INDEX idx_videojuego_categoria_filtro (categoria)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE videojuego_plataforma (
    videojuego_id BIGINT      NOT NULL,
    plataforma    VARCHAR(20) NOT NULL,
    PRIMARY KEY (videojuego_id, plataforma),
    CONSTRAINT fk_videojuego_plataforma_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    INDEX idx_videojuego_plataforma_filtro (plataforma)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
