# Puesta en marcha con datos de prueba

Pasos para arrancar LudusERP en local y que `DatosIniciales` cargue los datos de prueba.

## 1. Instalar JDK 21

El `pom.xml` pide Java 21. Descargar Temurin 21 desde https://adoptium.net y, en NetBeans:
*Propiedades del proyecto → Build → Compile → Java Platform* → elegir JDK 21.

## 2. Crear la clave AES

En PowerShell:

```powershell
$k = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
setx LUDUSERP_AES_KEY $k
echo $k
```

Guardar el valor que imprime `echo`. Si el grupo comparte volcados de datos, **todos deben usar la misma clave**.
La clave no va nunca en `application.properties` ni en `nbactions.xml`, que están versionados.

## 3. Reiniciar NetBeans

Cerrarlo del todo y volver a abrirlo. Las variables de entorno solo se leen al arrancar el proceso.

## 4. Arrancar Docker Desktop

Esperar a que indique que está en marcha. Sin Docker la aplicación no arranca.

## 5. Vaciar la base de datos (solo si ya se había arrancado antes)

Desde la carpeta del proyecto:

```powershell
docker compose down -v
```

Hace falta si:
- ya hay usuarios en la base (`DatosIniciales` no hace nada si existe alguno),
- se ha editado una migración ya aplicada (Flyway falla por checksum),
- se ha cambiado la clave AES (los datos cifrados ya no se pueden leer).

## 6. Ejecutar el proyecto

*Run* en NetBeans. Spring levanta los contenedores, Flyway crea las tablas (V1 a V6) y `DatosIniciales`
inserta departamentos, trabajadores, clientes y proveedores.

## 7. Comprobar los datos

Con Workbench:

| Campo         | Valor       |
|---------------|-------------|
| Host          | `localhost` |
| Puerto        | `3307`      |
| Usuario       | `luduserp`  |
| Contraseña    | `luduserp`  |
| Base de datos | `luduserp`  |

Revisar las tablas `usuario`, `trabajador`, `cliente` y `proveedor`. El NIF, el IBAN y el CIF aparecen
cifrados en Base64: es lo esperado.

## Aviso

Todavía no se puede iniciar sesión con los usuarios de prueba (`admin@luduserp.cat` / `luduserp`, etc.):
`SecurityConfig` usa usuarios en memoria (`admin/admin`) en vez de los de la base de datos.
