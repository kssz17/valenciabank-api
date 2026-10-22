# ValenciaBank API

API REST de un banco **simulado**: registro, login con JWT, consulta de saldo e historial, y
**transferencias con integridad transaccional**. La hice para aprender backend de verdad (Java +
Spring Boot + MySQL) y, sobre todo, para poder explicar cada decisión en una entrevista.

> ⚠️ **Es una demo.** Dinero de juguete, usuarios reales solo de una demo. No maneja dinero ni datos
> bancarios reales. Nunca metas contraseñas de otros servicios, tarjetas ni DNI reales.

## Qué hace

- **Registro y login** con contraseña hasheada (BCrypt) y token **JWT**.
- Cada usuario tiene una **cuenta** con saldo inicial simulado.
- Consultar **saldo** y **historial** de movimientos (paginado).
- **Transferir** dinero a otra cuenta del banco.
- Un **admin** puede listar usuarios e **ingresar** saldo de prueba.

La pieza central es la **transferencia**: atómica, validada y segura ante operaciones simultáneas.

## Stack y por qué

| Tecnología | Versión | Por qué |
|---|---|---|
| Java | 21 (LTS) | El LTS vigente que tengo instalado. |
| Spring Boot | 3.5.16 | Lo que las empresas usan hoy en producción. Boot 4 es muy nuevo. |
| MySQL | 8.4 | Base de datos relacional estándar (vía Docker). |
| Spring Security + JWT | — | Autenticación stateless y autorización por roles. |
| springdoc-openapi | 2.8.6 | Documentación interactiva (Swagger). |
| JUnit + Mockito | — | Tests de la lógica de negocio. |

## Arquitectura por capas

```
Cliente → Controller → Service → Repository → MySQL
          (HTTP)       (lógica)   (consultas)
```

- **controller** — recibe la petición HTTP y valida el formato. No tiene lógica de negocio.
- **service** — el cerebro: reglas, decisiones, transacciones.
- **repository** — habla con MySQL. Nada de lógica.
- **model** — entidades: `User`, `Account`, `Transaction`, `Role`.
- **dto** — objetos de entrada/salida (no expongo las entidades: así no filtro el hash de la contraseña).
- **config** — seguridad, filtro JWT, OpenAPI.

## Endpoints

| Método | Ruta | Acceso | Qué hace |
|---|---|---|---|
| POST | `/auth/register` | público | crear usuario + cuenta |
| POST | `/auth/login` | público | obtener token JWT |
| GET | `/accounts/me` | cliente | ver mi cuenta y saldo |
| GET | `/accounts/me/transactions` | cliente | mi historial (paginado) |
| POST | `/transfers` | cliente | transferir a otra cuenta |
| POST | `/admin/deposit` | admin | ingresar saldo simulado |
| GET | `/admin/users` | admin | listar usuarios |

## Cómo arrancarlo

### Opción rápida (todo con Docker)

```bash
docker compose up --build
```

Levanta **backend + MySQL** juntos. API en `http://localhost:8080`.

### Opción desarrollo (Java + Maven)

```bash
docker compose up -d mysql    # solo la base de datos
./mvnw spring-boot:run        # la API
```

- API en `http://localhost:8080`
- **Swagger (probar la API en el navegador):** `http://localhost:8080/swagger-ui.html`

### Frontend (Angular)

```bash
cd frontend
npm install
npm start        # http://localhost:4200
```

Despliegue en la nube: ver [DEPLOY.md](DEPLOY.md).

Para probar un endpoint protegido en Swagger: haz `POST /auth/login`, copia el `token`, pulsa
**Authorize** (arriba a la derecha) y pégalo.

## Tests

```bash
./mvnw test
```

Los tests corren sobre una base de datos **H2 en memoria** (no necesitan Docker). Incluyen los casos
límite de la transferencia: saldo insuficiente, transferencia a uno mismo y cuenta inexistente.

## Decisiones técnicas (el porqué)

- **El saldo es `BigDecimal`, nunca `double`.** `double` guarda en binario y pierde precisión
  (`0.1 + 0.2 = 0.30000000000000004`). En dinero es inaceptable; `BigDecimal` es exacto.
- **Transferencia `@Transactional`.** Restar de A y sumar a B pasan las dos o ninguna (rollback).
- **Concurrencia con bloqueo pesimista + orden consistente.** Dos transferencias simultáneas sobre
  la misma cuenta se serializan (`SELECT ... FOR UPDATE`), bloqueando siempre en orden de id
  ascendente para evitar deadlocks. Así el dinero nunca se crea ni se pierde.
- **Contraseñas con BCrypt**, nunca en texto plano (incluye *salt*).
- **JWT firmado** (no cifrado): nunca guardo datos sensibles dentro; la firma garantiza integridad.
- **DTOs** en vez de exponer entidades: para no devolver por accidente el hash de la contraseña.
- **Manejo global de errores** (`@RestControllerAdvice`): respuestas JSON limpias con el código correcto.

## Estado

App full-stack funcional: backend seguro (JWT + roles), testeado y documentado, frontend Angular, y
empaquetado con Docker. 

**Roadmap (mejoras futuras):** CI con GitHub Actions, migraciones con Flyway, tests de integración,
límite de transferencia diario, estado de la transacción e idempotencia.

---

Autor: Kevin Siabato · [github.com/kssz17](https://github.com/kssz17)
