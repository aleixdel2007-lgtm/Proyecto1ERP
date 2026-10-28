CREATE TABLE proveedor (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    nombre_comercial VARCHAR(120) NOT NULL,
    razon_social     VARCHAR(150) NOT NULL,
    tipo             VARCHAR(20)  NOT NULL,
    cuenta_id        BIGINT       NULL,
    cif              VARCHAR(128) NOT NULL,
    cif_hash         VARCHAR(64)  NOT NULL,
    iban             VARCHAR(128) NULL,
    email            VARCHAR(180) NOT NULL,
    telefono         VARCHAR(20)  NULL,
    pais             VARCHAR(60)  NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_proveedor_cif_hash UNIQUE (cif_hash),
    CONSTRAINT uk_proveedor_cuenta UNIQUE (cuenta_id),
    CONSTRAINT fk_proveedor_cuenta
        FOREIGN KEY (cuenta_id) REFERENCES cuenta_proveedor (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE compra (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    numero          VARCHAR(20)   NOT NULL,
    proveedor_id    BIGINT        NOT NULL,
    trabajador_id   BIGINT        NOT NULL,
    fecha           DATE          NOT NULL,
    fecha_recepcion DATE          NULL,
    estado          VARCHAR(20)   NOT NULL,
    total_coste     DECIMAL(12,2) NOT NULL,
    numero_albaran  VARCHAR(40)   NULL,
    observaciones   TEXT          NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_compra_numero UNIQUE (numero),
    CONSTRAINT fk_compra_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedor (id),
    CONSTRAINT fk_compra_trabajador
        FOREIGN KEY (trabajador_id) REFERENCES trabajador (id),
    INDEX idx_compra_estado (estado, fecha)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE linea_compra (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    compra_id         BIGINT        NOT NULL,
    videojuego_id     BIGINT        NOT NULL,
    cantidad          INT           NULL,
    cantidad_recibida INT           NULL,
    coste_unitario    DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_linea_compra_compra
        FOREIGN KEY (compra_id) REFERENCES compra (id),
    CONSTRAINT fk_linea_compra_videojuego
        FOREIGN KEY (videojuego_id) REFERENCES videojuego (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
