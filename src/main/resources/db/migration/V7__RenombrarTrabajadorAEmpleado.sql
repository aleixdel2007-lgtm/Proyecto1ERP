RENAME TABLE trabajador TO empleado;

ALTER TABLE empleado
    RENAME INDEX uk_trabajador_nif_hash TO uk_empleado_nif_hash;

ALTER TABLE empleado
    DROP FOREIGN KEY fk_trabajador_usuario;

ALTER TABLE empleado
    ADD CONSTRAINT fk_empleado_usuario
        FOREIGN KEY (id) REFERENCES usuario (id);

ALTER TABLE empleado
    DROP FOREIGN KEY fk_trabajador_departamento;

ALTER TABLE empleado
    ADD CONSTRAINT fk_empleado_departamento
        FOREIGN KEY (departamento_id) REFERENCES departamento (id);

ALTER TABLE compra
    DROP FOREIGN KEY fk_compra_trabajador;

ALTER TABLE compra
    CHANGE trabajador_id empleado_id BIGINT NOT NULL;

ALTER TABLE compra
    ADD CONSTRAINT fk_compra_empleado
        FOREIGN KEY (empleado_id) REFERENCES empleado (id);

ALTER TABLE solicitud_publicacion
    DROP FOREIGN KEY fk_solicitud_trabajador;

ALTER TABLE solicitud_publicacion
    CHANGE trabajador_id empleado_id BIGINT NULL;

ALTER TABLE solicitud_publicacion
    ADD CONSTRAINT fk_solicitud_empleado
        FOREIGN KEY (empleado_id) REFERENCES empleado (id);
