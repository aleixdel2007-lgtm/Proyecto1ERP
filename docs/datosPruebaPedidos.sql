-- Datos de prueba para ver el listado de pedidos (ventas/pedidos).
-- Crea 3 clientes, 3 videojuegos y 6 pedidos con sus lineas. Se puede pegar entero en MySQL.
-- Los clientes llevan un hash de relleno: sirven para ver datos, no para iniciar sesion.

-- Clientes
INSERT INTO usuario (email, password_hash, rol, nombre, apellidos, activo, debe_cambiar_password, fecha_alta) VALUES
    ('marta.ribas@demo.test', '$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ01234', 'CLIENTE', 'Marta', 'Ribas',  TRUE, FALSE, '2026-09-01 10:00:00'),
    ('luis.perez@demo.test',  '$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ01234', 'CLIENTE', 'Luis',  'Pérez',  TRUE, FALSE, '2026-09-05 12:30:00'),
    ('sara.vidal@demo.test',  '$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ01234', 'CLIENTE', 'Sara',  'Vidal',  TRUE, FALSE, '2026-09-10 18:15:00');

INSERT INTO cliente (id, telefono, direccion, ciudad, pais)
SELECT id, '600000000', 'Calle Demo 1', 'Barcelona', 'España'
FROM usuario
WHERE email IN ('marta.ribas@demo.test', 'luis.perez@demo.test', 'sara.vidal@demo.test');

-- Videojuegos. Van sin proveedor: el estudio ya no es texto libre, es videojuego.proveedor_id,
-- y estos tres son de prueba. En el listado del catalogo saldran con un guion en Estudio.
INSERT INTO videojuego (titulo, precio, pegi, activo, stock_ilimitado, umbral_stock_bajo) VALUES
    ('Demo Quest',   49.99, 12, TRUE, TRUE, 10),
    ('Demo Racing',  29.99,  3, TRUE, TRUE, 10),
    ('Demo Puzzle',  19.99,  3, TRUE, TRUE, 10);

-- Pedidos (IVA 21 %)
INSERT INTO pedido (numero, cliente_id, fecha, estado, base_imponible, iva, total, metodo_pago) VALUES
    ('C-2026-9001', (SELECT id FROM usuario WHERE email = 'marta.ribas@demo.test'), '2026-10-05 11:48:00', 'PAGADO',          49.99, 10.50,  60.49, 'Tarjeta'),
    ('C-2026-9002', (SELECT id FROM usuario WHERE email = 'marta.ribas@demo.test'), '2026-10-12 17:20:00', 'PAGADO',          79.98, 16.80,  96.78, 'Stripe'),
    ('C-2026-9003', (SELECT id FROM usuario WHERE email = 'luis.perez@demo.test'),  '2026-10-20 09:05:00', 'PENDIENTE_PAGO',  19.99,  4.20,  24.19, NULL),
    ('C-2026-9004', (SELECT id FROM usuario WHERE email = 'luis.perez@demo.test'),  '2026-10-22 20:40:00', 'REEMBOLSADO',     49.98, 10.50,  60.48, 'Tarjeta'),
    ('C-2026-9005', (SELECT id FROM usuario WHERE email = 'sara.vidal@demo.test'),  '2026-10-28 13:10:00', 'PAGADO',          99.97, 20.99, 120.96, 'PayPal'),
    ('C-2026-9006', (SELECT id FROM usuario WHERE email = 'sara.vidal@demo.test'),  '2026-11-03 16:55:00', 'CANCELADO',        29.99,  6.30,  36.29, NULL);

-- Lineas de pedido
INSERT INTO linea_pedido (pedido_id, videojuego_id, precio_unitario, descuento)
SELECT p.id, v.id, l.precio, 0.00
FROM (
    SELECT 'C-2026-9001' AS numero, 'Demo Quest'  AS titulo, 49.99 AS precio
    UNION ALL SELECT 'C-2026-9002', 'Demo Quest',  49.99
    UNION ALL SELECT 'C-2026-9002', 'Demo Racing', 29.99
    UNION ALL SELECT 'C-2026-9003', 'Demo Puzzle', 19.99
    UNION ALL SELECT 'C-2026-9004', 'Demo Racing', 29.99
    UNION ALL SELECT 'C-2026-9004', 'Demo Puzzle', 19.99
    UNION ALL SELECT 'C-2026-9005', 'Demo Quest',  49.99
    UNION ALL SELECT 'C-2026-9005', 'Demo Racing', 29.99
    UNION ALL SELECT 'C-2026-9005', 'Demo Puzzle', 19.99
    UNION ALL SELECT 'C-2026-9006', 'Demo Racing', 29.99
) l
JOIN pedido p ON p.numero = l.numero
JOIN videojuego v ON v.titulo = l.titulo;

-- Facturas (una por pedido cobrado; el reembolsado conserva la suya)
INSERT INTO factura (pedido_id, numero, fecha_emision, ruta_pdf)
SELECT p.id, f.factura, f.emision, NULL
FROM (
    SELECT 'C-2026-9001' AS pedido, 'F-2026-9001' AS factura, '2026-10-05' AS emision
    UNION ALL SELECT 'C-2026-9002', 'F-2026-9002', '2026-10-12'
    UNION ALL SELECT 'C-2026-9004', 'F-2026-9004', '2026-10-22'
    UNION ALL SELECT 'C-2026-9005', 'F-2026-9005', '2026-10-28'
) f
JOIN pedido p ON p.numero = f.pedido;

-- Para borrarlo todo despues (en este orden):
-- DELETE FROM factura WHERE numero LIKE 'F-2026-90%';
-- DELETE FROM linea_pedido WHERE pedido_id IN (SELECT id FROM pedido WHERE numero LIKE 'C-2026-90%');
-- DELETE FROM pedido WHERE numero LIKE 'C-2026-90%';
-- DELETE FROM videojuego WHERE titulo IN ('Demo Quest', 'Demo Racing', 'Demo Puzzle');
-- DELETE FROM cliente WHERE id IN (SELECT id FROM usuario WHERE email LIKE '%@demo.test');
-- DELETE FROM usuario WHERE email LIKE '%@demo.test';
