CREATE TABLE cesta (
    id                 BIGINT   NOT NULL AUTO_INCREMENT,
    cliente_id         BIGINT   NOT NULL,
    fecha_creacion     DATETIME NOT NULL,
    fecha_modificacion DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cesta_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_cesta_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE linea_cesta (
    id            BIGINT NOT NULL AUTO_INCREMENT,
    cesta_id      BIGINT NOT NULL,
    videojuego_id BIGINT NOT NULL,
    cantidad      INT    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_linea_cesta_videojuego UNIQUE (cesta_id, videojuego_id),
    CONSTRAINT fk_linea_cesta_cesta
        FOREIGN KEY (cesta_id) REFERENCES cesta (id),
    CONSTRAINT fk_linea_cesta_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE pedido (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    numero         VARCHAR(20)   NOT NULL,
    cliente_id     BIGINT        NOT NULL,
    fecha          DATETIME      NOT NULL,
    estado         VARCHAR(20)   NOT NULL,
    base_imponible DECIMAL(10,2) NOT NULL,
    iva            DECIMAL(10,2) NOT NULL,
    total          DECIMAL(10,2) NOT NULL,
    metodo_pago    VARCHAR(30)   NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    INDEX idx_pedido_cliente (cliente_id, fecha),
    INDEX idx_pedido_estado (estado, fecha)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE linea_pedido (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    pedido_id       BIGINT        NOT NULL,
    videojuego_id   BIGINT        NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    descuento       DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_linea_pedido_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_linea_pedido_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE factura (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    pedido_id     BIGINT      NOT NULL,
    numero        VARCHAR(20) NOT NULL,
    fecha_emision DATE        NOT NULL,
    ruta_pdf      VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_factura_pedido UNIQUE (pedido_id),
    CONSTRAINT uk_factura_numero UNIQUE (numero),
    CONSTRAINT fk_factura_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
