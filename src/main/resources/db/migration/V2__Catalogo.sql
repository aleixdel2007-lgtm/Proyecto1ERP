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

CREATE TABLE categoria (
    id     BIGINT      NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    activo BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_categoria_codigo UNIQUE (codigo),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE plataforma (
    id     BIGINT      NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    activo BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_plataforma_codigo UNIQUE (codigo),
    CONSTRAINT uk_plataforma_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE videojuego_categoria (
    videojuego_id BIGINT NOT NULL,
    categoria_id  BIGINT NOT NULL,
    PRIMARY KEY (videojuego_id, categoria_id),
    CONSTRAINT fk_vcat_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    CONSTRAINT fk_vcat_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    INDEX idx_videojuego_categoria_filtro (categoria_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE videojuego_plataforma (
    videojuego_id BIGINT NOT NULL,
    plataforma_id BIGINT NOT NULL,
    PRIMARY KEY (videojuego_id, plataforma_id),
    CONSTRAINT fk_vplat_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    CONSTRAINT fk_vplat_plataforma
        FOREIGN KEY (plataforma_id) REFERENCES plataforma (id),
    INDEX idx_videojuego_plataforma_filtro (plataforma_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Valores iniciales; despues se gestionan desde el CRUD.
INSERT INTO categoria (codigo, nombre) VALUES
    ('ACCION',     'Acción'),
    ('AVENTURA',   'Aventura'),
    ('RPG',        'RPG'),
    ('ESTRATEGIA', 'Estrategia'),
    ('SIMULACION', 'Simulación'),
    ('DEPORTES',   'Deportes'),
    ('TERROR',     'Terror'),
    ('INDIE',      'Indie'),
    ('CARRERAS',   'Carreras'),
    ('PUZZLE',     'Puzzle');

INSERT INTO plataforma (codigo, nombre) VALUES
    ('PC',  'PC'),
    ('PS5', 'PlayStation 5'),
    ('XSX', 'Xbox Series'),
    ('NSW', 'Nintendo Switch');
