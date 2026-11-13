-- El estudio de un juego no es texto libre: es el proveedor que nos lo suministra
-- (proveedor_id). Queda NULL en los juegos que sube un colaborador, que no se compran
-- a nadie y de los que el nombre sale de colaborador.nombre. La clave ajena se declara
-- en V3, que es donde nace la tabla proveedor.
--
-- precio es el precio estandar y precio_descuento lo que se cobra mientras haya rebaja:
-- NULL significa sin descuento.
--
-- fecha_publicacion es la primera vez que el juego se activo, y se sella una sola vez: si
-- luego se retira y se vuelve a publicar no cambia. NULL significa que no ha estado nunca
-- en la tienda. No es lo mismo que fecha_lanzamiento, que es la fecha oficial del juego y
-- la pone el gestor.
--
-- pendiente_revision marca los juegos de colaborador que esperan a que alguien los mire:
-- lo pone a TRUE el propio colaborador, al subir el juego y cada vez que lo edita, y lo
-- apaga el gestor al activarlo. Hace falta la columna porque el caso no se deduce de lo que
-- ya hay: activo a FALSE lo tiene tambien un juego que el gestor retiro a proposito, y la
-- version nueva solo aparece si el colaborador sube un ZIP, no si cambia la sinopsis o el
-- PEGI. Es este dato, y no fecha_publicacion, el que decide el distintivo del panel.
CREATE TABLE videojuego (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    titulo             VARCHAR(150)  NOT NULL,
    sinopsis           TEXT          NULL,
    proveedor_id       BIGINT        NULL,
    precio             DECIMAL(10,2) NOT NULL,
    precio_descuento   DECIMAL(10,2) NULL,
    fecha_lanzamiento  DATE          NULL,
    pegi               INT           NOT NULL,
    activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_publicacion  DATETIME      NULL,
    pendiente_revision BOOLEAN       NOT NULL DEFAULT FALSE,
    stock_ilimitado    BOOLEAN       NOT NULL DEFAULT FALSE,
    umbral_stock_bajo  INT           NOT NULL DEFAULT 10,
    PRIMARY KEY (id),
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
    PRIMARY KEY (id),
    CONSTRAINT uk_categoria_codigo UNIQUE (codigo),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE plataforma (
    id     BIGINT      NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(60) NOT NULL,
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
