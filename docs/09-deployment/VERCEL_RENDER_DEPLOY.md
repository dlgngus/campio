# Vercel + Render Deploy

Use Vercel for `campio-frontend` and Render for `campio-backend` plus PostgreSQL.

## 1. Render Backend

Create a new Render Blueprint from this repository.

Render will read:

```text
render.yaml
```

The Blueprint creates:

- `campio-backend` Docker web service
- `campio-postgres` PostgreSQL database

During Blueprint setup, Render prompts for these secret values:

```text
FRONTEND_ORIGIN=https://your-vercel-domain.vercel.app
CAMPIO_ADMIN_EMAIL=your-admin-email@example.com
CAMPIO_ADMIN_PASSWORD=strong-password
```

Keep these enabled:

```text
CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true
CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true
```

After deploy, check:

```bash
curl https://your-render-service.onrender.com/api/health
curl https://your-render-service.onrender.com/api/opportunities
```

## 2. Vercel Frontend

Create a Vercel project from the same repository.

Use:

```text
Framework Preset: Vite
Root Directory: campio-frontend
Build Command: npm run build
Output Directory: dist
```

Set:

```text
VITE_API_BASE_URL=https://your-render-service.onrender.com
```

The SPA fallback is already configured in:

```text
campio-frontend/vercel.json
```

## 3. Final Render CORS Update

After Vercel gives the final production URL, update Render:

```text
FRONTEND_ORIGIN=https://your-vercel-domain.vercel.app
```

Redeploy the Render backend after changing this value.

## 4. Smoke Test

In the browser:

- Open the Vercel URL.
- Open `/explore`.
- Open one opportunity detail page.
- Sign up with a real email/password.
- Log out and log back in.
- Save and unsave one opportunity.

Expected production data after the first backend startup:

- K-Startup public startup opportunities
- 기업마당 public government support opportunities
- Ambiguous deadline records stay in `raw_opportunities` and do not appear publicly until reviewed.
