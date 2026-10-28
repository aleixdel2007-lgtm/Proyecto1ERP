CREATE TABLE clave_licencia (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    videojuego_id    BIGINT       NOT NULL,
    linea_compra_id  BIGINT       NOT NULL,
    cliente_id       BIGINT       NULL,
    linea_pedido_id  BIGINT       NULL,
    codigo_cifrado   VARCHAR(128) NOT NULL,
    codigo_hash      VARCHAR(64)  NOT NULL,
    estado           VARCHAR(20)  NOT NULL,
    fecha_alta       DATETIME     NOT NULL,
    fecha_reserva    DATETIME     NULL,
    fecha_venta      DATETIME     NULL,
    fecha_anulacion  DATETIME     NULL,
    motivo_anulacion VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_clave_licencia_codigo_hash UNIQUE (codigo_hash),
    CONSTRAINT uk_clave_licencia_linea_pedido UNIQUE (linea_pedido_id),
    CONSTRAINT fk_clave_licencia_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id),
    CONSTRAINT fk_clave_licencia_linea_compra
        FOREIGN KEY (linea_compra_id) REFERENCES linea_compra (id),
    CONSTRAINT fk_clave_licencia_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_clave_licencia_linea_pedido
        FOREIGN KEY (linea_pedido_id) REFERENCES linea_pedido (id),
    INDEX idx_clave_licencia_stock (videojuego_id, estado),
    INDEX idx_clave_licencia_biblioteca (cliente_id, estado)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
