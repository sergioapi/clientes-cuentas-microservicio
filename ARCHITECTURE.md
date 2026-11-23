# Arquitectura y decisiones técnicas

Este documento resume los componentes, las dependencias y las decisiones de diseño del microservicio. Las instrucciones
de ejecución y el uso de la API se encuentran en el [README](README.md).

## 1. Arquitectura hexagonal

El desarrollo se planteó con un enfoque **API First**: se definió inicialmente el contrato `openapi.yaml` y se mantuvo
actualizado durante la implementación.

La aplicación se organiza en tres áreas:

- **`domain`**: modelos `Cliente` y `CuentaBancaria` como *records* de Java, con sus invariantes y sin dependencias de
  Spring ni JPA.
- **`application`**: casos de uso, servicios y puertos de entrada y salida. Los servicios dependen de interfaces, no de
  implementaciones de persistencia.
- **`infrastructure`**: controladores REST, DTOs, adaptadores JPA, gestión de transacciones y configuración.

Las peticiones pasan de los controladores a los casos de uso, que utilizan puertos para acceder a los datos. Las
**dependencias del código** se dirigen hacia el dominio y las interfaces de aplicación; `ApplicationConfig` ensambla las
implementaciones concretas.

También se abstraen las transacciones mediante `TransactionPort`, implementado con `TransactionTemplate`, para mantener
los servicios de aplicación independientes de Spring.

## 2. Modelo de datos y persistencia

Un cliente se identifica mediante su **DNI** y puede tener varias cuentas bancarias. Cada cuenta tiene un ID generado y
una referencia obligatoria a su titular. Las claves primaria y foránea protegen la integridad referencial.

**Flyway** versiona la creación del esquema y la carga inicial; Hibernate valida las entidades JPA frente a ese esquema.
La aplicación utiliza **H2 en memoria**, como establece el ejercicio, por lo que los cambios no persisten entre
reinicios.

Los saldos se representan mediante **`BigDecimal`** en Java y **`DECIMAL(19,2)`** en SQL, en vez de `Double`. Se evita
así la pérdida de precisión asociada a la representación binaria de importes decimales.

## 3. Rendimiento y acceso a datos

Las consultas realizan el trabajo de filtrado y agregación en **SQL**, evitando cargar tablas enteras para procesarlas
en Java:

- **Mayoría de edad:** filtro por fecha de nacimiento, con un límite calculado mediante un `Clock` inyectado; se
  excluyen las fechas desconocidas.
- **Saldo acumulado:** agrupación de cuentas por cliente y condición `HAVING SUM(...) > cantidad`, respetando la
  comparación estricta del enunciado.
- **Listados:** paginación y orden ascendente por DNI en la base de datos, con 20 clientes por defecto y un máximo de
  100 por página.
- **Prevención de N+1:** las cuentas de una página de clientes se recuperan en una consulta por lote (`IN`), en vez de
  una por cliente. El detalle utiliza `LEFT JOIN FETCH` para obtener cliente y cuentas conjuntamente.

La **paginación predeterminada** se introdujo deliberadamente, aunque el enunciado describe un listado completo, para
limitar el tamaño de las respuestas y el consumo de memoria ante un volumen elevado de datos. Los resultados completos
pueden recorrerse página a página.

Este límite afecta al número de clientes, **no al de cuentas de cada cliente**. Asimismo, las páginas profundas con
`OFFSET` y las agregaciones extensas pueden incrementar el trabajo de la base de datos. Son compromisos conocidos; no se
atribuye al servicio una capacidad concreta de carga sin mediciones.

## 4. Transacciones, integridad y concurrencia

En `POST /cuentas`, la creación eventual del cliente y el guardado de su cuenta forman **una única transacción**: si
falla una escritura, se revierte toda la operación. `PUT /cuentas/{idCuenta}` sustituye el saldo existente y también se
ejecuta transaccionalmente.

Se detectó una carrera cuando dos POST simultáneos consultaban el mismo DNI inexistente e intentaban insertar al
cliente. La clave primaria impedía duplicarlo, pero una petición podía devolver `409` aunque ambas cuentas fueran
válidas.

La solución identifica **únicamente la violación de unicidad del DNI** durante el `insert` del cliente. Después del
*rollback*, el servicio realiza **un solo reintento de la operación completa** en una nueva transacción: vuelve a
consultar el DNI, encuentra al cliente ya confirmado y guarda la segunda cuenta. No se reintentan otros errores de
persistencia.

Esta recuperación se comprobó con tests de integración y con una reproducción concurrente real sobre H2: ambos POST
devolvieron `201` y quedaron un cliente y dos cuentas distintas.

El mecanismo trata esa colisión concreta; **no garantiza idempotencia** ante peticiones POST repetidas tras un
*timeout*, ni cubre todos los errores de bloqueo. El reintento está diseñado para el límite transaccional actual de las
peticiones HTTP; una futura invocación desde una transacción exterior requeriría revisar la propagación.