# Quitar los DTO y dejar las vistas con lo conocido

Preparado el 30 de octubre de 2026. Decision tomada: en este proyecto **no hay DTO**.
El formulario trabaja contra la entidad, las validaciones se escriben a mano en el
service y los errores viajan en excepciones propias. El modelo es el
`DepartamentoServiceImpl` del equipo y el proyecto `Project1`.

Este documento no cambia nada por si solo: es la lista de lo que hay que tocar.

---

## 1. Inventario de atributos Thymeleaf

Comparado `Project1` (13 plantillas) con lo que hay aqui.

| Atributo | En Project1 | Veredicto |
|---|---|---|
| `th:href` `th:text` `th:each` `th:if` `th:action` `th:object` `th:field` `th:value` | si | se queda |
| `th:selected` `th:disabled` `th:src` `th:formaction` | si | se queda |
| `th:classappend` | **si, 3 veces** | se queda |
| `${#lists}` `${#strings}` | si | se queda |
| `th:unless` | no | **sustituible** por `th:if` negado |
| `th:errors` y `${#fields...}` | no | **desaparecen solos** al quitar los DTO |
| `th:replace` `th:fragment` `~{...}` | no | se queda: ya estaba aqui antes (commit 3f4acd2) |
| `sec:authorize` `sec:authentication` `${#authentication}` | no | se queda: Project1 no tiene Spring Security |

Dos aclaraciones sobre esta tabla:

**`th:classappend` si se usa en Project1**, y en una forma mas complicada que ninguna de
las de aqui: `Incidencias/listarIncidencias.html` lineas 68, 73 y 81, con ternarios
anidados, navegacion segura `?.` y una referencia de tipo `T(entity.EstatIncidencia)`.

**Los fragmentos no son nuevos de esta sesion.** `th:replace` con `~{...}` entro en el
commit `3f4acd2` "Navbar como fragmento y home la usa", y `home.html`, `tienda.html`,
`compras/proveedores.html` y `compras/proveedor.html` ya los usaban. Project1 no los tiene
porque repite la barra de navegacion completa en cada una de sus 13 plantillas.

Lo que de verdad se va de las vistas es **la validacion por anotaciones**: los 29
`${#fields...}` y los 12 `th:errors`, que existen solo porque hay DTO.

---

## 2. Departamentos

### 2.1 Se borra

- `src/main/java/dto/DepartamentoFormulario.java`

### 2.2 `service/interfaces/DepartamentoService.java`

Vuelve a la firma del equipo, con un solo cambio de tipo en el listado:

    List<Departamento> listarDepartamentos();
    Departamento buscarDepartamentoXId(Long id);
    Departamento guardarDepartamento(Departamento departamento, Long responsableId);
    void eliminarDepartamento(Long id);
    List<Trabajador> listarTrabajadores();

### 2.3 `service/implement/DepartamentoServiceImpl.java`

Se queda tal como esta, con cuatro retoques:

1. `listarDepartamentos()` llama a `departamentoDao.listarConDetalle()` en vez de
   `findAll()`. Obligatorio: la vista lee `d.responsable.nombre` y `d.trabajadores`, las
   dos LAZY, y con `spring.jpa.open-in-view=false` eso es `LazyInitializationException`.
2. `buscarDepartamentoXId` lanza `DepartamentoNoEncontradoException` en vez de la
   `EntityNotFoundException` de jakarta, que sale como 500.
3. `validarNombreRepetido` cambia el `findAll()` con bucle por
   `existsByNombreIgnoreCase` / `existsByNombreIgnoreCaseAndIdNot`. Opcional: con cuatro
   departamentos la version actual funciona igual, es solo eficiencia.
4. Metodo privado nuevo, obligatorio, porque hoy es un 500 sin mensaje:

        private void validarResponsableLibre(Departamento departamento, Long responsableId) {

            if (responsableId == null) {
                return;
            }

            boolean ocupado;

            if (departamento.getId() == null) {
                ocupado = departamentoDao.existsByResponsableId(responsableId);
            } else {
                ocupado = departamentoDao.existsByResponsableIdAndIdNot(responsableId, departamento.getId());
            }

            if (ocupado) {
                throw new ResponsableOcupadoException();
            }
        }

   Se llama desde `guardarDepartamento`, antes de `asignarResponsable`.

Las dos excepciones genericas que ya lanza pasan a propias:
`IllegalArgumentException` del nombre repetido a `NombreDepartamentoDuplicadoException`, y
`IllegalStateException` del borrado a `DepartamentoConTrabajadoresException`.

`departamentoDao.save(departamento)` directo **se queda**: el formulario trae los tres
campos que tiene la entidad, asi que no hay nada que perder.

### 2.4 `controller/ControllerDepartamento.java`

Su estructura es la correcta. Lo unico que falta es capturar, que es el motivo real de que
hoy los mensajes acaben en un 500:

    @PostMapping("/departamentos/guardar")
    public String guardarDepartamento(@ModelAttribute("departamento") Departamento departamento,
                                      @RequestParam(required = false) Long responsableId,
                                      Model model) {
        try {
            departamentoService.guardarDepartamento(departamento, responsableId);
        } catch (NombreDepartamentoDuplicadoException | ResponsableOcupadoException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("trabajadores", departamentoService.listarTrabajadores());

            if (departamento.getId() == null) {
                return "departamentos/formularioNuevoDep";
            }
            return "departamentos/formularioEditarDep";
        }

        return "redirect:/departamentos";
    }

    @PostMapping("/departamentos/eliminar/{id}")
    public String eliminarDepartamento(@PathVariable Long id, RedirectAttributes flash) {
        try {
            departamentoService.eliminarDepartamento(id);
        } catch (DepartamentoConTrabajadoresException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/departamentos";
    }

El `model.addAttribute("trabajadores", ...)` del catch no es un adorno: al volver a la
plantilla sin redirigir, el desplegable se quedaria vacio sin eso.

### 2.5 Las vistas

**Los tres ficheros, el mismo cambio de nombre:** `${formulario}` pasa a `${departamento}`
en los dos `th:object`. Los `th:field` de dentro (`*{nombre}`, `*{descripcion}`, `*{id}`)
no se tocan, siguen funcionando igual.

**En los dos formularios, el desplegable** vuelve al patron del equipo y de Project1
(`Incidencias/editarIncidencia.html`, linea 79), sin `th:field`:

    <select class="inp" name="responsableId">
        <option value="">Sin asignar</option>
        <option th:each="trabajador : ${trabajadores}"
                th:value="${trabajador.id}"
                th:text="${trabajador.nombre} + ' ' + ${trabajador.apellidos}"
                th:selected="${departamento.responsable != null and departamento.responsable.id == trabajador.id}">
        </option>
    </select>

En el formulario de nuevo, el `th:selected` se puede omitir.

**En los dos formularios, fuera los bloques de error por campo.** Esto se va entero:

    <span class="hint err" th:if="${#fields.hasErrors('nombre')}" th:errors="*{nombre}"></span>
    <span class="hint" th:unless="${#fields.hasErrors('nombre')}">Unico en la tabla.</span>

y el `th:classappend="${#fields.hasErrors('nombre')} ? 'bad' : ''"` de cada `input`.
El mensaje lo da la linea que ya esta puesta arriba del formulario:

    <div class="alert alert-err" th:if="${error}" th:text="${error}"></div>

El `<span class="hint">Unico en la tabla.</span>` se puede dejar como texto fijo, sin
condicion: es informativo, no un error.

**Los `required` de los `input` obligatorios se quedan**: sin `@Valid`, son la primera
barrera, igual que en `Project1/Trabajadores/crearTrabajador.html`.

**Opcional, si quereis solo atributos de Project1:** los `th:unless` del listado y de los
formularios se cambian por `th:if` negado. Son 11 en departamentos:

    th:unless="${#lists.isEmpty(departamentos)}"   ->   th:if="${!#lists.isEmpty(departamentos)}"
    th:unless="${d.responsable}"                   ->   th:if="${d.responsable == null}"

El resto del listado ya esta con lo conocido: `th:each`, `th:text`, `th:href`,
`${#lists.size(...)}` y un ternario.

---

## 3. Proveedores

### 3.1 Se borra

- `src/main/java/dto/ProveedorFormulario.java`

### 3.2 `service/interfaces/ProveedorService.java`

Mismos nombres que la de departamentos, para que el proyecto tenga un solo estilo:

    List<Proveedor> listarProveedores();
    Proveedor buscarProveedorXId(Long id);
    Proveedor guardarProveedor(Proveedor proveedor);
    void cambiarActivo(Long id, boolean activo);

Desaparecen `buscarFormulario` y `aFormulario`, que era un detalle de mapeo publicado en
la interfaz.

### 3.3 `service/implement/ProveedorServiceImpl.java`

Se reescribe con la forma del de departamentos: metodo publico corto, `validarXxx` privado
debajo, `if/else` en vez de ternario, y sin `@Transactional` ni
`@RequiredArgsConstructor`.

    @Service
    public class ProveedorServiceImpl implements ProveedorService {

        @Autowired
        private ProveedorDao proveedorDao;

        @Override
        public List<Proveedor> listarProveedores() {
            return proveedorDao.findAllByOrderByNombreComercialAsc();
        }

        @Override
        public Proveedor buscarProveedorXId(Long id) {

            Proveedor proveedor = proveedorDao.findById(id).orElse(null);

            if (proveedor == null) {
                throw new ProveedorNoEncontradoException(id);
            }
            return proveedor;
        }

        @Override
        public Proveedor guardarProveedor(Proveedor proveedor) {

            validarCifRepetido(proveedor);

            proveedor.setCifHash(IndiceCiego.hash(proveedor.getCif()));

            return proveedorDao.save(proveedor);
        }

        private void validarCifRepetido(Proveedor proveedor) {

            String hash = IndiceCiego.hash(proveedor.getCif());
            boolean repetido;

            if (proveedor.getId() == null) {
                repetido = proveedorDao.existsByCifHash(hash);
            } else {
                repetido = proveedorDao.existsByCifHashAndIdNot(hash, proveedor.getId());
            }

            if (repetido) {
                throw new CifDuplicadoException();
            }
        }

        @Override
        public void cambiarActivo(Long id, boolean activo) {

            Proveedor proveedor = buscarProveedorXId(id);
            proveedor.setActivo(activo);
            proveedorDao.save(proveedor);
        }
    }

Dos lineas de este fichero no son obvias y conviene no borrarlas por error:

**`setCifHash`.** La columna `cif` va cifrada con AES-GCM, que no es determinista: dos
cifrados del mismo CIF dan textos distintos. Sobre esa columna no se puede buscar ni poner
`UNIQUE`. El hash es lo que sostiene el `uk_proveedor_cif_hash` que ya esta en
`V3__Aprovisionamiento.sql`.

**El `save` de `cambiarActivo`.** Hoy funciona sin el porque la clase tiene
`@Transactional`. Al quitarlo, sin `save` el cambio no llega a la base.

### 3.4 `controller/ControllerProveedor.java`

- fuera `@Valid` y `BindingResult`; el `@ModelAttribute` pasa a ser `Proveedor`
- el `catch` de `CifDuplicadoException` manda el mensaje al modelo y devuelve el
  formulario, en vez de `errores.rejectValue`
- **arreglar el fallo que tiene hoy**: `editarProveedor` devuelve
  `"compras/proveedores"`, que es el listado, en vez de `"compras/proveedor"`. Por eso el
  boton de editar no funciona
- `@RequestMapping("/compras/proveedores")` y el `@ModelAttribute("tipos")` se quedan

### 3.5 `templates/compras/proveedor.html`

- `th:object="${formulario}"` pasa a `th:object="${proveedor}"`, y los dos
  `${formulario.id} == null ? ... : ...` del titulo y del boton pasan a `${proveedor.id}`
- fuera los **ocho** bloques `th:if="${#fields.hasErrors(...)}"` con su `th:errors`, y los
  **nueve** `th:classappend="${#fields.hasErrors(...)} ? 'bad' : ''"`
- arriba del formulario, la misma linea que en departamentos:
  `<div class="alert alert-err" th:if="${error}" th:text="${error}"></div>`
- los `<span class="hint">` informativos ("Se almacena cifrado", "Tambien cifrado") se
  quedan como texto fijo
- **anadir `required`** a nombre comercial, razon social, CIF y correo, que hoy no lo
  llevan porque confiaban en `@Valid`

### 3.6 `templates/compras/proveedores.html`

No cambia nada: no usa `#fields` ni DTO, solo `th:each`, `th:text`, `th:href`,
`th:classappend` y `${#lists}`.

---

## 4. Las excepciones

Cuatro clases, en `exception`, de seis lineas cada una. Solo la de "no encontrado" lleva
`@ResponseStatus`; las otras tres las captura el controlador y nunca deben salir como
error HTTP.

| Clase | Mensaje | Estado |
|---|---|---|
| `DepartamentoNoEncontradoException` | "No existe el departamento N" | ya creada |
| `NombreDepartamentoDuplicadoException` | "Ya existe un departamento con ese nombre" | por crear |
| `ResponsableOcupadoException` | "Ese trabajador ya es responsable de otro departamento" | por crear |
| `DepartamentoConTrabajadoresException` | "No se puede eliminar un departamento con trabajadores asociados" | por crear |

En proveedores ya estan `ProveedorNoEncontradoException` y `CifDuplicadoException`.

---

## 5. Decision pendiente

`Proveedor` tiene un `@OneToOne` a `CuentaProveedor` que el formulario no lleva. Al guardar
la entidad que viene del POST, Hibernate hace `merge` y ese `cuenta_id` se pone a **nulo**:
los dos proveedores ESTUDIO de `DatosIniciales` perderian su cuenta al editarlos.

**Opcion A.** En la edicion, cargar el proveedor existente y copiarle los campos uno a uno
en un metodo privado `copiarDatos`. Es lo que hace Project1 en
`ControllerIncidencias.guardarEdicionIncidencia`, que carga `existente` y copia campo a
campo para no perder la imagen. Se copia una vez, no dos como con el DTO.

**Opcion B.** Dejar el `save` directo y aceptar que editar un ESTUDIO desvincula su cuenta.

En departamentos este problema no existe: el formulario trae todos los campos de la entidad.

---

## 6. Fuera de este plan

- La paginacion del listado de departamentos (punto 14 de `cambiosPendientesDepartamentos.md`)
- `@EnableMethodSecurity` no esta, asi que un `@PreAuthorize` se ignoraria en silencio
- `/compras/**` sigue abierto a cualquier usuario con sesion: un CLIENTE o un PROVEEDOR
  entra en el CRUD y ve los CIF y los IBAN descifrados
