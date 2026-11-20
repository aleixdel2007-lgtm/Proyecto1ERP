# LudusERP

ERP de distribución de videojuegos con tienda para el cliente final. Por un lado el panel
interno —catálogo, proveedores, órdenes de compra, claves de licencia, pedidos y facturas,
clientes, empleados y departamentos, más un panel de control que resume los números del
negocio—, y por otro la tienda, donde el cliente compra, recibe su clave, la guarda en su
biblioteca, se descarga el juego y lo valora. Los colaboradores publican sus propios juegos,
que un gestor revisa antes de que salgan al catálogo.

Todo el panel y la tienda están en castellano, inglés y catalán.

## Documentación

La entrega completa está en `docs/Documentació final/`, en PDF:

| Documento | Qué cuenta |
|---|---|
| `GuiaInstalacion` | Requisitos, la clave de cifrado, el arranque y los errores típicos con su solución |
| `ManualUsuario` | Qué ve cada rol y cómo se hace cada cosa, pantalla por pantalla |
| `Arquitectura` | Las capas, las decisiones técnicas con su porqué, y lo que quedó fuera |

El resto de `docs/`: el diseño de clases y de la base de datos en `LudusERP-Disseny-UML.pdf`, los
resúmenes por tema en `resumenes/` —cada uno en PDF y en HTML, los HTML dentro de
`resumenes/html/`— y el prototipo visual del panel en `prototipo.html`.

## Tecnologías

| Pieza | Qué se usa |
|---|---|
| Base | Spring Boot 4.1.1, Java 21 |
| Vistas | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Datos | MySQL 8.4 con JPA/Hibernate y migraciones Flyway |
| Traducciones | MongoDB 8.0 (`MongoMessageSource`) |
| Seguridad | Spring Security, 7 roles |
| Avisos | WebSocket (STOMP) sobre `/topic` |
| Pago | Stripe |
| Correo | Spring Mail contra Mailpit |
| Tareas | `@Scheduled`: cada 5 minutos cancela los pedidos pendientes de pago que han caducado |
| Facturas | openhtmltopdf |

## Requisitos

- **Java 21**
- **Docker**, encendido antes de arrancar
- **Maven**. El proyecto no trae wrapper (`mvnw`), así que hace falta Maven instalado o
  usar el que viene con NetBeans

## La clave de cifrado

El NIF y el IBAN del empleado, el CIF y el IBAN del proveedor y el código de cada clave de
licencia se guardan cifrados con AES-256-GCM. La clave maestra **no está en el repositorio**:
se lee de la variable de entorno `LUDUSERP_AES_KEY`, y sin ella la aplicación no arranca.

Son 32 bytes en Base64. Para generarla:

```bash
openssl rand -base64 32
```

Dos avisos que ahorran una tarde:

- Todo el grupo necesita **la misma clave** si os pasáis volcados de la base: lo cifrado con
  una no se descifra con otra.
- En Windows, después de crear la variable hay que **reiniciar NetBeans entero**, no solo el
  proyecto, o seguirá sin verla.

## Arrancar

```bash
mvn spring-boot:run
```

No hace falta levantar nada a mano. Spring lee `compose.yaml`, arranca MySQL, MongoDB y
Mailpit, espera a que pasen el healthcheck e inyecta las URL de conexión — por eso en
`application.properties` no hay ni usuario ni contraseña de base de datos.

| Servicio | Dónde |
|---|---|
| Aplicación | http://localhost:8082 |
| Mailpit (correo de prueba) | http://localhost:8025 |
| MySQL | `localhost:3307`, usuario y contraseña `luduserp` |
| MongoDB | `localhost:27018`, usuario y contraseña `luduserp` |

Los puertos de las bases no son los estándar a propósito, para no chocar con un MySQL o un
Mongo que ya tengas instalado. Al cerrar la aplicación los contenedores **siguen encendidos**
(`lifecycle-management=start_only`), así que el siguiente arranque no espera. Para pararlos:
`docker compose stop`.

## Usuarios de prueba

Con el perfil `dev` activo, `config/DatosIniciales` siembra la demo si la base está vacía.
Todos tienen la misma contraseña: **`luduserp`**.

| Correo | Rol |
|---|---|
| `admin@luduserp.cat` | ADMIN |
| `gestor.productos@luduserp.cat` | GESTOR_PRODUCTOS |
| `gestor.clientes@luduserp.cat` | GESTOR_CLIENTES |
| `gestor.inventario@luduserp.cat` | GESTOR_INVENTARIO |
| `ana.torres@example.com` | CLIENTE |
| `jordi.bastida@example.com` | COLABORADOR |

Fuera del perfil `dev` manda `config/DatosAdmin`, que sobre una base sin usuarios crea
únicamente el administrador. Su correo sale de `app.admin.email` y su contraseña de
`app.admin.password`; si esa contraseña no está configurada, la aplicación no arranca, para
que no quede nunca un admin con una contraseña conocida.

## Contraseñas y bloqueo de cuenta

Cada usuario se cambia la contraseña desde su perfil (`POST /user/changePassword`). Hay que
acertar la actual, y la nueva necesita ocho caracteres, una mayúscula, un símbolo y ser
distinta de la vieja. El registro del cliente pide ocho caracteres con mayúscula y número.

**No hay cambio obligatorio en el primer acceso.** La marca `debe_cambiar_password` que lo
preparaba se eliminó el 20/11/2026 —columna, campo, servicios y vistas incluidos— porque
nadie la leía y el cambio desde el perfil cubre el caso.

A los **cinco intentos fallidos** la cuenta se desactiva sola y la aplicación le escribe el
motivo, que el gestor ve al pasar el ratón por el badge INACTIVO del listado de clientes. Un
acierto antes del quinto devuelve el contador a cero. El límite vive en un solo sitio,
`UsuarioServiceImpl.MAX_INTENTOS`, y el texto del motivo lo toma de ahí. Al reactivar la
cuenta se borran el motivo y el contador; al desactivarla a mano, el gestor tiene que
escribir el motivo, que es obligatorio.

## Base de datos y migraciones

El esquema lo construye Flyway desde `src/main/resources/db/migration` (`V1` a `V10`).
Hibernate va con `ddl-auto=validate`: no toca el esquema, solo comprueba que las entidades
cuadren con las tablas y se niega a arrancar si no. Una entidad nueva sin su migración
reventará el arranque, y es queriendo.

Convención del proyecto: cuando hay que cambiar el esquema **se edita la migración que ya
existe**, no se añade una nueva. El esquema se lee de un tirón, pero cambia el checksum de
esa migración, así que cada cambio obliga a recrear la base.

**Si alguien edita una migración ya aplicada**, Flyway se quejará del checksum al siguiente
arranque. La salida buena es recrear la base:

```bash
docker compose down -v
```

Eso borra los volúmenes, así que vuelve a correr todo desde `V1` y se siembra la demo otra
vez. Es también la única forma de ver datos sembrados nuevos: `DatosIniciales` solo escribe
cuando la base está vacía.

## Traducciones

Las claves viven en `src/main/resources/i18n/textos_es|en|ca.properties`, con **el mismo
juego de claves en los tres**. Al arrancar, `TraduccionesIniciales` vuelca a Mongo las que
falten sin pisar las que ya están, y desde ahí las sirve `MongoMessageSource`.

- Después de añadir una clave hay que **reiniciar** para que llegue a Mongo.
- En catalán, si una clave lleva `{0}`, los apóstrofos van **doblados** (`''`), porque
  `MessageFormat` se los come. Sin `{0}` no hace falta.
- Convención: el mensaje de una excepción **es la clave de traducción**, no el texto. El
  controlador la resuelve con `messageSource.getMessage(...)`.

## Tests

```bash
mvn test
```

Son 22 pruebas en cuatro clases. `AccesoTest`, `ClientesTest` y `PanelControlTest` levantan
solo su controlador con `@WebMvcTest`; `PanelServiceTest` prueba el servicio del panel con
Mockito. Ninguna necesita base de datos ni Docker, y los textos se leen de los `.properties`,
así que una clave que falte hace fallar la prueba.

Están enganchadas a la fase `process-classes`, de modo que **se ejecutan también al arrancar
la aplicación**: si una se rompe, el arranque se para antes de levantar nada.

## Estructura

```
src/main/java/
  application/   la clase main
  config/        seguridad, WebSocket, cifrado, siembra de datos, tareas programadas
  controller/    un controlador por módulo
  service/       interfaces/ e implement/
  dao/           repositorios de Spring Data (JPA y Mongo)
  model/         entidades, por módulo de negocio
  exception/     excepciones propias, agrupadas por módulo

src/main/resources/
  db/migration/  las migraciones de Flyway
  i18n/          los tres .properties
  templates/     Thymeleaf
  static/        css, js e imágenes

src/test/java/test/  las pruebas y la clase de arranque que necesitan

docs/            diseño UML, documentación final y resúmenes
```

Las clases y las tablas van en castellano; los paquetes, en inglés.

## Archivos subidos

Las imágenes de perfil y de los juegos se guardan en `uploads/`, y los ZIP de los juegos de
colaborador en `juegos-privados/`, fuera de `uploads/` a propósito: todo lo que cuelga de ahí
lo sirve Spring como recurso público, y un ZIP de pago no puede estar al alcance de
cualquiera. Lo entrega un controlador que comprueba antes quién lo pide.

Las dos carpetas están en `.gitignore`, así que no viajan en el repositorio: si recreas la
base, las filas que apuntaban a esos archivos desaparecen y los archivos se quedan huérfanos
en disco.

## Pagos

El pago se hace con Stripe Checkout y la clave secreta sale de la variable
`STRIPE_SECRET_KEY`. Sin ella la aplicación arranca igual, pero al intentar pagar falla. Los
datos de la tarjeta se teclean siempre en Stripe, nunca en un formulario de la aplicación, y
los precios que se cobran salen de nuestra base de datos, no de lo que llegue del navegador.
