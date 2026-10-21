# ValenciaBank API

API REST de un banco **simulado**. Dinero de juguete, usuarios reales de una demo. La hice para
aprender backend de verdad (Java + Spring Boot + MySQL) y, sobre todo, para poder explicar cada
decisión en una entrevista.

> ⚠️ **Es una demo.** No maneja dinero ni datos bancarios reales. No pidas ni metas contraseñas de
> otros servicios, datos de tarjetas ni DNI reales: todo son datos de prueba.

## Qué hace (MVP)

- Registro e inicio de sesión (login seguro con token JWT).
- Cada usuario tiene una cuenta con un saldo inicial simulado.
- Consultar saldo y ver el historial de movimientos.
- **Transferir** dinero a otra cuenta del banco.
- Un rol admin puede listar usuarios e "ingresar" saldo de prueba.

La pieza central es la **transferencia**: que el dinero nunca se cree ni se pierda, ni siquiera con
operaciones simultáneas.

## Stack y por qué

| Tecnología | Versión | Por qué |
|---|---|---|
| Java | 21 (LTS) | Es el LTS que tengo instalado y el más moderno. 17 también valía; no tenía sentido fijar una versión más vieja. |
| Spring Boot | 3.5.16 | Es lo que las empresas tienen hoy en producción (banca/consultoría). Boot 4 ya existe, pero casi nadie lo usa aún; para empleabilidad, 3.x es la apuesta segura. |
| MySQL | — | Base de datos relacional estándar. El dinero vive en tablas con relaciones claras. |
| Maven | wrapper 3.9.9 | Uso el *wrapper* (`mvnw`) para no depender de tener Maven instalado en la máquina. |

## Estructura por capas

```
controller  → recibe la petición HTTP, valida el formato. NO tiene lógica de negocio.
service     → aquí vive la lógica (reglas, decisiones, transacciones).
repository  → habla con MySQL. NADA de lógica.
model       → las entidades (User, Account, Transaction, Role).
dto         → objetos de entrada/salida de la API (no expongo las entidades directamente).
config      → configuración (seguridad, CORS, etc.).
```

La regla: cada capa solo habla con la de al lado. El controller no sabe de base de datos y el
repository no sabe de HTTP. Si cambio la BD, no toco la lógica.

## Decisiones técnicas

- **El saldo es `BigDecimal`, nunca `double`.** Con `double` los decimales pierden céntimos por cómo
  se representan en binario — inaceptable en algo que mueve dinero.
- **Contraseñas con BCrypt**, nunca en texto plano.
- **DTOs** en vez de exponer las entidades: así no devuelvo por accidente el hash de la contraseña.
- *(Esta sección irá creciendo bloque a bloque, con el porqué de cada decisión.)*

## Cómo arrancarlo

> Pendiente de documentar cuando conectemos la base de datos (Bloque 2). De momento:

```bash
./mvnw clean compile
```

## Estado

🚧 En construcción, bloque a bloque. Hecho hasta ahora: **Bloque 0 — fundación del proyecto**.

---

Autor: Kevin Siabato · [github.com/kssz17](https://github.com/kssz17)
