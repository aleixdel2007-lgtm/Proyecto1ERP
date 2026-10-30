# Cambios pendientes del CRUD de departamentos

Revision del 30 de octubre de 2026. Ordenados de mas importante a menos.
El punto 1 de la lista original (cerrar las rutas) **ya esta aplicado**: ver el apartado
"Hecho" al final.

Ficheros implicados:

| Capa      | Fichero                                              |
|-----------|------------------------------------------------------|
| controlador | `controller/ControllerDepartamento.java`           |
| servicio  | `service/interfaces/DepartamentoService.java`, `service/implement/DepartamentoServiceImpl.java` |
| dao       | `dao/DepartamentoDao.java`, `dao/TrabajadorDao.java` |
| vistas    | `templates/departamentos/listado.html`, `formularioNuevoDep.html`, `formularioEditarDep.html` |

---

## 1. Dejar de usar la entidad como `@ModelAttribute`

`guardarDepartamento` recibe un `Departamento` directamente desde el formulario, asi que
cualquier campo de la entidad es escribible desde un POST manipulado, y cambiando el `id`
oculto se edita un departamento ajeno.

Hace falta un `DepartamentoFormulario` en `dto`, con `@NotBlank` y `@Size(max = 80)` sobre
el nombre y `@Size(max = 255)` sobre la descripcion, mas `@Valid` y `BindingResult` en el
controlador. Al hacerlo desaparece `validarNombre()` del servicio, que hoy repite en Java
lo que ya dicen la entidad y la tabla.

## 2. Que los errores lleguen al usuario

El servicio lanza `IllegalArgumentException` e `IllegalStateException` y el controlador no
captura nada, de modo que "Ya existe un departamento con ese nombre" y "No se puede
eliminar un departamento con trabajadores asociados" acaban en una pagina de error 500.

Capturarlos: `errores.rejectValue("nombre", ...)` para el duplicado y un flash de error con
`RedirectAttributes` para el borrado. `ControllerProveedor` ya lo hace asi y sirve de modelo.

## 3. Excepcion propia para el "no existe"

`buscarDepartamentoXId` usa la `EntityNotFoundException` de jakarta, que sale como 500.
El patron del proyecto es una excepcion propia con `@ResponseStatus(HttpStatus.NOT_FOUND)`,
igual que `exception/ProveedorNoEncontradoException`.

## 4. Validar el nombre duplicado en la base, no en memoria

`validarNombreRepetido` se trae todos los departamentos con `findAll()` y los recorre.
Debe ser `existsByNombreIgnoreCase` y `existsByNombreIgnoreCaseAndIdNot` en
`DepartamentoDao`, como hace `ProveedorDao` con el `cif_hash`.

Conviene capturar ademas `DataIntegrityViolationException` como ultima red: el
`uk_departamento_nombre` de la tabla sigue ahi y una carrera entre dos peticiones lo saltaria.

## 5. Validar el responsable

`asignarResponsable` no comprueba que ese trabajador no sea ya responsable de otro
departamento, y la tabla tiene `uk_departamento_responsable UNIQUE`: asignar uno ocupado es
un 500 sin mensaje.

DECISION PENDIENTE: el desplegable ofrece hoy todos los trabajadores de la empresa. Hay que
decidir si el responsable debe pertenecer al departamento que dirige (lo razonable es que si)
y filtrar la lista en consecuencia.

## 6. `@Transactional` donde toca

`guardarDepartamento` no es transaccional, asi que la comprobacion de duplicado y el `save`
no son atomicos. Lo coherente con `ProveedorServiceImpl` es `@Transactional(readOnly = true)`
en la clase y `@Transactional` en los metodos que escriben.

## 7. Inyeccion por constructor

`ControllerDepartamento` y `DepartamentoServiceImpl` usan `@Autowired` sobre campos. El
acuerdo del grupo (apartado 5 de `memoriaClaudeByAleix.txt`) es `@RequiredArgsConstructor`.

## 8. Guardar copiando campos sobre la entidad cargada

Hoy el `save` en edicion recibe una entidad construida desde el formulario, por lo que hace
`merge` y lo que no venga en el formulario queda a nulo. Ahora no rompe nada porque
Departamento solo tiene tres campos, pero el dia que crezca el formulario le borrara lo que
no enseñe. Con el DTO del punto 1 se resuelve: cargar, copiar, guardar.

## 9. Las vistas, con el diseño del resto

Las tres plantillas van sin CSS y sin fragmentos, mientras `compras/proveedores` ya usa
`panel.css` con `fragments/panel :: menu`, `fragments/panel :: barra` y `fragments/iconos`.
Es el "falta el css" del commit 3e26193.

`formularioEditarDep.html` tiene todavia `<title>TODO supply a title</title>`.

## 10. Entrada en el menu

`fragments/panel.html` solo tiene Inicio y Proveedores: a `/departamentos` no se llega
navegando, hay que escribir la URL. Al añadirla, envolverla en `sec:authorize="hasRole('ADMIN')"`
para que no la vean los roles que ya no pueden entrar.

## 11. Completar el listado

No muestra responsable ni numero de trabajadores, no tiene estado vacio ("todavia no hay
departamentos") ni sitio donde pintar el `mensaje` de exito o el error del punto 2.

## 12. Ojo con `numeroTrabajadores()` si se saca en el listado

`Departamento.numeroTrabajadores()` recorre la coleccion LAZY. Con
`spring.jpa.open-in-view=false`, llamarlo desde una plantilla es `LazyInitializationException`.
Si se quiere la columna hace falta un `count` en el DAO o una proyeccion, no el metodo de la
entidad.

## 13. Quitar los comentarios de NetBeans

Cabeceras de licencia y `@author Sandy` en el controlador, el DAO, la interfaz, el impl y las
tres plantillas, mas algun comentario de linea. Los comentarios los pone Aleix al terminar
los objetos.

## 14. Paginacion

El listado los saca todos. Con cuatro departamentos da igual, pero `compras/proveedores` ya
tiene el hueco del paginador puesto.

---

## Hecho el 30 de octubre

**Rutas cerradas.** En `config/SecurityConfig.java`, dentro de `authorizeHttpRequests`:

    .requestMatchers("/departamentos/**").hasRole("ADMIN")

Antes `/departamentos/**` no tenia regla y caia en `anyRequest().authenticated()`: cualquier
usuario logueado, incluido un CLIENTE o un PROVEEDOR, podia crear, editar y borrar
departamentos.

Verificado arrancando la aplicacion:

| Quien              | GET /departamentos | GET /departamentos/nuevo | POST guardar |
|--------------------|--------------------|--------------------------|--------------|
| anonimo            | 302 a /login       | 302 a /login             | -            |
| ADMIN              | 200                | 200                      | -            |
| CLIENTE            | 403                | 403                      | 403          |
| GESTOR_PRODUCTOS   | 403                | 403                      | 403          |

**Renombrado del DAO.** `dao/DepartamentoDAO.java` pasa a `dao/DepartamentoDao.java`: el
fichero no se llamaba igual que el tipo publico que declara y eso impedia compilar el
proyecto entero (90 errores, de los que 89 eran Lombok sin ejecutar por culpa del primero).

---

## Dos avisos que no son de departamentos

- `@PreAuthorize` TODAVIA NO HACE NADA en este proyecto: `SecurityConfig` no tiene
  `@EnableMethodSecurity` y Spring Boot no la activa sola. Si se pone la anotacion hoy se
  ignora en silencio, con la falsa sensacion de estar protegido. La convencion 4.3 pide las
  dos capas, asi que habra que activarla cuando se quiera la segunda.
- `/compras/**` tiene exactamente el mismo agujero que se acaba de cerrar aqui: el CRUD de
  proveedores esta abierto a cualquiera que haya iniciado sesion.
