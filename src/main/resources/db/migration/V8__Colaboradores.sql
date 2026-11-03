-- El estudio que publica deja de ser un proveedor con cuenta y pasa a ser una cara
-- mas de usuario, al lado de cliente y empleado: el COLABORADOR.
--
-- Su nombre y su correo ya viven en usuario, asi que esta tabla solo anade lo suyo
-- y comparte la clave primaria, igual que cliente y empleado. Proveedor vuelve a ser
-- solo la empresa a la que se le compran claves, y por eso pierde el tipo y la cuenta:
-- a un colaborador no se le compra nada.
--
-- Y como el colaborador sube el juego el mismo, sin que nadie lo apruebe, desaparece
-- el modulo de solicitudes entero. version_juego se queda: ahi vive el ZIP y ya cuelga
-- del videojuego.
--
-- El orden de aqui abajo importa: se copia antes de borrar, y las claves ajenas se
-- sueltan antes de tocar las columnas a las que apuntan.

CREATE TABLE colaborador (
    id   BIGINT      NOT NULL,
    pais VARCHAR(60) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_colaborador_usuario
        FOREIGN KEY (id) REFERENCES usuario (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Cada cuenta de proveedor se convierte en un colaborador con el mismo id de usuario,
-- y se queda con el pais que tenia su ficha de proveedor.
INSERT INTO colaborador (id, pais)
SELECT cp.id, p.pais
FROM cuenta_proveedor cp
         LEFT JOIN proveedor p ON p.cuenta_id = cp.id;

UPDATE usuario SET rol = 'COLABORADOR' WHERE rol = 'PROVEEDOR';

-- El colaborador sube su juego directamente, sin solicitud que nadie revise, asi que
-- el juego necesita saber quien lo subio. Vacio en los juegos de distribuidor, relleno
-- en los de colaborador: es tambien el filtro del apartado de la tienda. No se declara
-- un indice aparte porque la clave ajena ya crea el suyo sobre esta columna.
ALTER TABLE videojuego
    ADD COLUMN colaborador_id BIGINT NULL;

ALTER TABLE videojuego
    ADD CONSTRAINT fk_videojuego_colaborador
        FOREIGN KEY (colaborador_id) REFERENCES colaborador (id);

-- La clave de un juego de colaborador se acuna al comprarlo y no viene de ninguna
-- compra, asi que deja de ser obligatorio tener linea de compra detras. Quien publico
-- el juego se sabe por videojuego.colaborador_id, y sin coste el margen es el precio
-- integro: el mismo resultado que buscaba la compra a coste 0, sin la ficcion.
ALTER TABLE clave_licencia
    MODIFY linea_compra_id BIGINT NULL;

-- Fuera el modulo de solicitudes entero. Las hijas antes que la madre, y todo esto
-- antes de borrar los proveedores de tipo ESTUDIO, porque sus solicitudes los apuntan.
DROP TABLE imagen_solicitud;

DROP TABLE solicitud_categoria;

DROP TABLE solicitud_plataforma;

DROP TABLE solicitud_publicacion;

-- Proveedor suelta la cuenta: ese vinculo es justo lo que deja de existir.
ALTER TABLE proveedor
    DROP FOREIGN KEY fk_proveedor_cuenta;

ALTER TABLE proveedor
    DROP INDEX uk_proveedor_cuenta;

ALTER TABLE proveedor
    DROP COLUMN cuenta_id;

DELETE FROM proveedor WHERE tipo = 'ESTUDIO';

ALTER TABLE proveedor
    DROP COLUMN tipo;

DROP TABLE cuenta_proveedor;
