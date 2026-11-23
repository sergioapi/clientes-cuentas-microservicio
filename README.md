# Microservicio de gestión de clientes y cuentas bancarias

API REST desarrollada con Java 21 y Spring Boot 3 para consultar clientes, gestionar sus cuentas bancarias y actualizar
saldos. El proyecto implementa los seis endpoints solicitados en el ejercicio, siguiendo arquitectura hexagonal (puertos
y adaptadores).

La aplicación utiliza H2 en memoria, con datos iniciales cargados automáticamente, e incluye un contrato OpenAPI que
puede consultarse desde Swagger UI.

## Requisitos

- **JDK 21** instalado y configurado (`java -version`).
- No es necesario instalar Maven si se utiliza el **Maven Wrapper** incluido en el repositorio.
- Puerto **8080** disponible, salvo que se cambie la configuración.

Tecnologías principales: Spring Boot 3, Spring Web, Spring Data JPA, H2, Flyway, Bean Validation, Lombok, Maven y JUnit
5 / MockMvc.

## Ejecutar la aplicación

Desde la raíz del repositorio:

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

Al arrancar, Flyway aplica las migraciones SQL y carga los datos iniciales: **5 clientes y 7 cuentas bancarias**. Como
H2 funciona en memoria, los cambios realizados mediante la API se pierden cuando se detiene la aplicación.

### Documentación interactiva y base de datos

| Recurso          | Dirección                                     |
|------------------|-----------------------------------------------|
| Swagger UI       | `http://localhost:8080/swagger-ui/index.html` |
| Contrato OpenAPI | `http://localhost:8080/openapi.yaml`          |
| Consola H2       | `http://localhost:8080/h2-console`            |

Para acceder a la consola H2, utiliza los valores de conexión (`spring.datasource.url`, usuario y contraseña) definidos
en `src/main/resources/application.yaml`. La consola está destinada al desarrollo local.

## Endpoints

| Método | Ruta                                         | Descripción                                                                       |
|--------|----------------------------------------------|-----------------------------------------------------------------------------------|
| `GET`  | `/clientes`                                  | Lista clientes con sus cuentas bancarias.                                         |
| `GET`  | `/clientes/mayores-de-edad`                  | Lista clientes de 18 años o más, sin cuentas en la respuesta.                     |
| `GET`  | `/clientes/con-cuenta-superior-a/{cantidad}` | Lista clientes cuya **suma de saldos** supera estrictamente la cantidad indicada. |
| `GET`  | `/clientes/{dni}`                            | Consulta un cliente por DNI con sus cuentas.                                      |
| `POST` | `/cuentas`                                   | Crea una cuenta y, si hace falta, también el cliente con el DNI indicado.         |
| `PUT`  | `/cuentas/{idCuenta}`                        | **Sustituye** el saldo de la cuenta por el importe recibido; no suma ni resta.    |

Los endpoints de listado admiten `page` (por defecto `0`) y `size` (por defecto `20`, máximo `100`). Los resultados se
ordenan por DNI ascendente. Si una página no contiene resultados, se devuelve una lista vacía.

Para el formato completo de las peticiones, respuestas, validaciones y códigos HTTP,
consulta [OpenAPI](src/main/resources/static/openapi.yaml) o Swagger UI.

## Ejemplos de peticiones

Los siguientes comandos usan `curl` (terminal Linux/macOS o Git Bash). También se pueden ejecutar desde la opción **Try
it out** de Swagger UI.

**Consultar clientes y sus cuentas:**

```bash
curl "http://localhost:8080/clientes"
```

**Consultar una página concreta:**

```bash
curl "http://localhost:8080/clientes?page=1&size=2"
```

**Consultar clientes mayores de edad:**

```bash
curl "http://localhost:8080/clientes/mayores-de-edad"
```

**Filtrar clientes por suma de saldos:**

```bash
curl "http://localhost:8080/clientes/con-cuenta-superior-a/50000"
```

**Consultar un cliente por DNI:**

```bash
curl "http://localhost:8080/clientes/11111111A"
```

**Crear una cuenta para un cliente nuevo:**

```bash
curl -i -X POST "http://localhost:8080/cuentas" \
  -H "Content-Type: application/json" \
  -d '{"dniCliente":"66666666F","tipoCuenta":"NORMAL","total":100.25}'
```

Si el cliente no existe, se crea con su DNI y con los demás datos personales sin informar. La nueva cuenta se devuelve
con estado **`201 Created`**.

**Actualizar el saldo de una cuenta existente:**

```bash
curl -i -X PUT "http://localhost:8080/cuentas/1" \
  -H "Content-Type: application/json" \
  -d '{"total":200.35}'
```

El importe recibido reemplaza el saldo anterior. La respuesta devuelve la cuenta actualizada con estado **`200 OK`**.

### Validaciones y errores

- DNI: ocho dígitos y una letra mayúscula; no se comprueba la letra de control.
- Tipo de cuenta: texto no vacío, hasta 50 caracteres; se eliminan espacios exteriores y se normaliza a mayúsculas.
- Saldo: importe decimal con hasta 17 dígitos enteros y dos decimales. Se permiten importes negativos, cero y positivos.
- Las entradas inválidas producen `400`; los recursos inexistentes, `404`. Los conflictos de integridad pueden producir
  `409`.
- Las respuestas se sirven como JSON. Una cabecera `Accept` incompatible puede producir `406`; un cuerpo POST/PUT con
  tipo no admitido, `415`.

Los detalles de error se documentan mediante `ProblemDetail` en el contrato OpenAPI.

## Ejecutar los tests

Desde la raíz del proyecto:

**Windows (PowerShell):**

```powershell
.\mvnw.cmd clean verify
```

**Linux / macOS:**

```bash
./mvnw clean verify
```

La suite incluye pruebas de dominio, servicios de aplicación, persistencia y controladores REST con MockMvc. Se
comprueban, entre otros casos, los seis endpoints, validaciones, transacciones y paginación.

## Arquitectura y decisiones técnicas

El código se organiza según el patrón **hexagonal**:

- `domain`: modelos y reglas de negocio, sin dependencias de Spring/JPA.
- `application`: casos de uso y puertos de entrada/salida.
- `infrastructure`: controladores REST, mapeos, adaptadores JPA, transacciones y configuración.

Consulta [ARCHITECTURE.md](ARCHITECTURE.md) para conocer las decisiones relativas a consultas SQL, paginación,
integridad y concurrencia.
