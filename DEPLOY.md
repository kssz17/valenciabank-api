# Despliegue de ValenciaBank (gratis, para una demo)

Son 3 piezas: **frontend** (Vercel), **backend** (Railway) y **base de datos** MySQL (Railway).
Orden: primero el backend + base de datos, luego el frontend (necesita la URL del backend).

> Nota honesta: los planes gratuitos pueden "dormir" el backend tras un rato sin uso (la primera
> visita tarda unos segundos en despertar) y tienen límites. Para una demo con amigos, sobra.

---

## Paso 1 — Backend + base de datos en Railway

1. Entra en [railway.app](https://railway.app) y regístrate con tu GitHub.
2. **New Project** → **Deploy from GitHub repo** → elige `kssz17/valenciabank-api`.
   Railway detecta el `Dockerfile` y construye el backend.
3. En el proyecto, **New** → **Database** → **Add MySQL**. Railway crea la BD y sus variables.
4. Abre el servicio del **backend** → pestaña **Variables** → añade estas (referencian la BD de Railway):

   | Variable | Valor |
   |---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}` |
   | `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
   | `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
   | `JWT_SECRET` | una clave larga e inventada (mínimo 32 caracteres) |
   | `ADMIN_EMAIL` | tu email de admin |
   | `ADMIN_PASSWORD` | una contraseña de admin |
   | `APP_CORS_ORIGINS` | lo rellenas en el Paso 3 con tu URL de Vercel |

5. En **Settings** del backend → **Networking** → **Generate Domain**. Copia la URL pública
   (ej. `https://valenciabank-api-production.up.railway.app`). **Esa es la URL de tu backend.**
6. Comprueba que vive: abre `https://TU-BACKEND.up.railway.app/swagger-ui.html`.

## Paso 2 — Conectar el frontend al backend

1. En `frontend/src/environments/environment.prod.ts`, pon tu URL del backend en `apiUrl`.
2. Haz commit y push (eso disparará el build de Vercel en el Paso 3).

## Paso 3 — Frontend en Vercel

1. Entra en [vercel.com](https://vercel.com) y regístrate con GitHub.
2. **Add New** → **Project** → importa `kssz17/valenciabank-api`.
3. En la configuración del proyecto:
   - **Root Directory**: `frontend`
   - Framework: **Angular** (lo detecta solo).
4. **Deploy**. Al terminar, copia tu URL de Vercel (ej. `https://valenciabank.vercel.app`).
5. Vuelve a **Railway** → variable `APP_CORS_ORIGINS` del backend → ponla a:
   `https://TU-APP.vercel.app` (tu URL de Vercel). Railway redeploya el backend.

## Listo

Comparte la URL de Vercel con tu familia y amigos. 🎉 Se registran, usan la demo, y tú mides
cuántos la usaron (eso lo cuentas en el CV).

## Local (como hasta ahora)

```bash
docker compose up -d mysql
./mvnw spring-boot:run
# en otra terminal:
cd frontend && npm start
```
