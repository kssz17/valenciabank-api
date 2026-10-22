# Despliegue de ValenciaBank

## Opción A — Local con Docker (todo con un comando)

Levanta **backend + MySQL** juntos:

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

Para pararlo: `docker compose down` (añade `-v` para borrar también los datos).

> El `docker-compose.yml` pasa la configuración por variables de entorno. Para producción,
> cambia `JWT_SECRET`, `DB_PASSWORD` y `ADMIN_PASSWORD` por valores propios (nunca los subas al repo).

## Opción B — En la nube (para que tu familia la use por internet)

La idea: **backend + MySQL** en un sitio, **frontend** en otro.

### Backend + MySQL (Railway o Render)
1. Crea una cuenta en [Railway](https://railway.app) o [Render](https://render.com).
2. Conecta tu repo de GitHub `kssz17/valenciabank-api`.
3. Añade un servicio de **MySQL** (lo ofrecen gestionado).
4. Despliega el backend desde el `Dockerfile`. Configura las variables de entorno:
   - `SPRING_DATASOURCE_URL` → la URL del MySQL gestionado.
   - `DB_USERNAME`, `DB_PASSWORD` → las del MySQL gestionado.
   - `JWT_SECRET` → una clave larga y secreta (NUEVA, no la de desarrollo).
   - `ADMIN_EMAIL`, `ADMIN_PASSWORD` → tu admin.
5. Anota la URL pública del backend (ej. `https://valenciabank-api.up.railway.app`).

### Frontend (Vercel)
1. En `frontend/src/app/core/models.ts`, cambia `API_URL` por la URL pública del backend.
2. En el backend, añade esa URL de Vercel a los orígenes permitidos de CORS (`SecurityConfig`).
3. Sube el frontend a [Vercel](https://vercel.com) (detecta Angular automáticamente).
4. Comparte el enlace de Vercel con amigos y familia. 🎉

## Medir usuarios reales (para el CV)

Pide a quien la use que se registre y haga transferencias de juguete entre ellos. Cuenta cuántos
usuarios y operaciones hubo: *"demo funcional con N usuarios reales que realizaron N operaciones"*.
