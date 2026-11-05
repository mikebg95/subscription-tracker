# Subscription Tracker — Frontend

A minimal single-page application (SPA) for the Subscription Tracker, built from the
[UI designs](../docs/design) on top of the [backend API](../backend/README.md).

## Tech stack

| Layer | Choice |
|---|---|
| Framework | Vue 3 (Composition API, `<script setup>`) |
| Routing | Vue Router |
| Build / dev server | Vite |
| HTTP | Native `fetch` — no client library |
| Styling | Plain CSS, colors taken from the designs, light & dark mode |

## What it does

One screen, as designed:

- **Home** — total number of subscriptions, total monthly cost, and the list of subscriptions.
- **Empty state** — when there are no subscriptions yet.
- **Add / edit dialog** — with inline validation that mirrors the backend's rules (name required,
  max 100 characters; price required, greater than 0, max 8 digits and 2 decimals). A duplicate
  name (`409`) is shown on the name field.
- **Delete dialog** — asks for confirmation first.
- **Something went wrong** — when the API can't be reached.
- **Page not found** — for any unknown URL.

## Getting started

### Prerequisites

- Node.js 20.19+ (or 22.12+)
- The backend running on `http://localhost:8080` — see the [backend README](../backend/README.md)
  (`./mvnw spring-boot:test-run` needs no database setup)

### Run it

```bash
npm install
npm run dev
```

The app comes up on `http://localhost:5173`. In dev, Vite proxies every `/api` call to the backend
on port 8080, so the browser only talks to one origin and no CORS setup is needed.

### Build for production

```bash
VITE_API_BASE_URL=https://api.example.com npm run build
```

The static files are written to `dist/`. `VITE_API_BASE_URL` is the backend's address, without
`/api/v1`. Leave it out when the frontend and backend are served from the same domain. When they
are on different domains, add the frontend's URL to the backend's `CORS_ALLOWED_ORIGINS`.

Because this is an SPA with history-mode routing, the web server must serve `index.html` for
unknown paths, so the "Page not found" screen can handle them.

## Project layout

```
frontend/
├── index.html
├── vite.config.js            # dev server + /api proxy
├── public/favicon.svg
└── src/
    ├── main.js
    ├── App.vue               # header + router view
    ├── router.js             # home + not-found routes
    ├── styles.css
    ├── format.js             # euro formatting
    ├── api/subscriptions.js  # fetch calls to /api/v1/subscriptions
    ├── components/           # icons, add/edit dialog, delete dialog
    └── views/                # home, not found
```
