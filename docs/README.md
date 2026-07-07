# Subscription Tracker — Architecture

Architecture-as-code for the Subscription Tracker system: the **C4 model** (via the
**Structurizr DSL**) and the **database schema** (via **DBML**). Both are the source of truth for
the system's design, versioned alongside the code they describe.

## System overview

Subscription Tracker is a personal-finance tool for tracking recurring software subscriptions and
calculating monthly spend. It is designed as three containers:

| Container | Technology | Status |
|---|---|---|
| Frontend Web Application | Angular | Planned |
| Backend API | Java / Spring Boot | Implemented |
| Database | PostgreSQL | Implemented |

![C4 System Context](c4/system-context-view.png)

## C4 model

The architecture is written as code in [`c4/workspace.dsl`](c4/workspace.dsl) and rendered with
Structurizr.

### Static views

| View | Scope |
|---|---|
| System Context | The user and the Subscription Tracker system |
| Container | Frontend, Backend API, and Database, and how they interact |
| Component | The Backend API's internals — Controller, Service, DAO |

![C4 Container view](c4/container-view.png)

![C4 Component view](c4/component-view-backend.png)

### Dynamic views

Per-flow sequences through the Backend API, defined in the DSL and rendered on demand in
Structurizr:

- **Success flows** — Create, Get all, Get by id, Update, Delete, Count, Calculate total
- **Error flows** — Create with duplicate name (409), Get by id not found (404), Create with
  validation failure (400)

### Rendering the diagrams locally

Prerequisite: [Docker](https://www.docker.com/products/docker-desktop/).

```bash
cd c4
docker run -it --rm -p 9090:8080 -v "$(pwd)":/usr/local/structurizr structurizr/structurizr local
```

Open <http://localhost:9090> and switch between the views. Static PNG exports of the three core
views are committed under `c4/` for quick reference without running Structurizr.

## Database schema

The schema is defined as code in [`database/schema.dbml`](database/schema.dbml) (source of truth),
with a rendered diagram at [`database/db-diagram.png`](database/db-diagram.png).

![Database schema](database/db-diagram.png)

To edit, paste `schema.dbml` into [dbdiagram.io](https://dbdiagram.io); it renders live and can
export PostgreSQL DDL or a PNG.

## Repository structure

```
subscription-tracker-architecture/
├── c4/
│   ├── workspace.dsl              # C4 model — source of truth
│   ├── workspace.json             # Structurizr layout & state
│   ├── system-context-view.png    # exported static views
│   ├── container-view.png
│   └── component-view-backend.png
└── database/
    ├── schema.dbml                # DB schema — source of truth
    └── db-diagram.png             # rendered schema diagram
```

Part of the [`subscription-tracker`](https://github.com/mikebg95/subscription-tracker) monorepo,
alongside the backend and frontend modules.
