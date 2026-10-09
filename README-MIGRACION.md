# ProyectoPPI — backend (proyectodb)

Este proyecto fue generado con Spring Initializr (con Maven Wrapper incluido) y aquí se le
agregó el código migrado desde el backend original en Node/Express + `mssql`.

## Qué se agregó sobre el esqueleto de Initializr

- **`src/main/resources/application.properties`**: se le agregó la configuración de conexión a
  SQL Server, el secreto JWT y los datos del usuario administrador, todo leído desde variables
  de entorno con valores por defecto iguales a los del `.env` original (`DB_USER`/`DB_PASSWORD`
  en vez de `USER`/`PASSWORD`, ya que `USER` es una variable reservada del sistema operativo).
- **`pom.xml`**: se agregaron las dependencias `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (JWT) y
  `spring-security-crypto` (BCrypt), que no están en el catálogo de Initializr.
- **`src/main/java/com/proyectoPPI/proyectodb/`**:
  - `controllers/` → `AuthController`, `UsuariosController`, `CursosController`,
    `LeccionesController` (equivalentes a los controladores de Express).
  - `security/` → `JwtUtil`, `AuthInterceptor`, `RequireAuth`, `RequireAdmin` (equivalentes a
    `auth.middleware.js`).
  - `db/` → `Db` (equivalente a `config/db.js`, con el chequeo de disponibilidad de conexión).
  - `config/` → `CorsConfig`, `WebConfig`, `GlobalExceptionHandler`.
  - `ProyectodbApplication.java`: se le agregó un `CommandLineRunner` que verifica la conexión
    y crea el usuario administrador al arrancar (equivalente a lo que hacía `server.js`).

Las rutas, códigos de estado y JSON de respuesta se mantienen exactamente iguales a los del
backend original en Node — el frontend en React no necesita ningún cambio.

## Cómo correrlo

1. Copia `.env.example` como referencia y define esas mismas variables en tu entorno (o en el
   Run Configuration de tu IDE): `DB_USER`, `DB_PASSWORD`, `SERVER`, `DATABASE`, `DB_PORT`,
   `JWT_SECRET`, `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NOMBRE`,
   `ADMIN_APELLIDO`, `PORT`.
2. Como este proyecto trae el Maven Wrapper, no necesitas tener Maven instalado:
   - Linux/Mac: `./mvnw spring-boot:run`
   - Windows: `mvnw.cmd spring-boot:run`
3. El frontend en `ProyectoPPI-Frontend` sigue apuntando al mismo puerto/rutas, sin cambios.

## Nota importante

Este proyecto usa **Spring Boot 4.1.1** (la versión que trajo tu Initializr), un poco más nueva
que la 3.3.4 que usé en la primera entrega. La API de `JdbcTemplate`, `HandlerInterceptor`,
`WebMvcConfigurer` y las anotaciones REST usadas aquí no cambiaron entre esas versiones, así que
el código migrado no tuvo que ajustarse por eso — solo se adaptaron los nombres de starter que
Initializr ya generó distinto (`spring-boot-starter-webmvc` en vez de `spring-boot-starter-web`).

Como en la entrega anterior, este sandbox no tiene salida a Maven Central, así que no pude
compilar el proyecto para verificarlo end-to-end. Te recomiendo correr `./mvnw clean package`
(o `mvnw.cmd clean package` en Windows) antes de darlo por definitivo.
