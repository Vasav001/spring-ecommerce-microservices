# Spring Ecommerce Microservices

An event-driven shop built from Spring Boot services. They talk over Kafka and use an orchestrated order saga, Keycloak for login and a React frontend.

Live: https://34-8-188-125.sslip.io

## Services

| Service | Port | Responsibility |
|---|---|---|
| customer-service | 8081 | Profile and addresses. The profile is created on first request from the Keycloak user id. |
| catalog-service | 8082 | Products and reviews. Only delivered orders can be reviewed. |
| inventory-service | 8083 | Stock levels, plus reserve and release for orders |
| order-service | 8084 | Cart, checkout and the order saga |
| integration-service | 8086 | Dummy payments and shipping (the user decides pass or fail) and Cloudinary upload signatures |
| search-service | 8085 | Denormalized product search with filters, built from events |
| frontend | 5173 (dev) | Vite + React + TypeScript |

Each service owns its own Postgres database, migrated with Flyway.

## Kafka topics

| Topic | Producer | Consumers |
|---|---|---|
| catalog.products | catalog | inventory, search |
| inventory.stock-levels | inventory | search |
| review.product-ratings | catalog | search |
| order.events | order | catalog, integration |
| inventory.commands | order | inventory |
| inventory.replies | inventory | order |
| payment.commands | order | integration |
| payment.replies | integration | order |
| shipment.events | integration | order |

Every message is written through a transactional outbox (`outbox-lib`), so a database change and the message it causes are committed together. Consumers record handled event ids in `processed_event`, so a message delivered twice is only applied once.

### Order saga

1. Checkout creates a PENDING order and tells inventory to reserve stock.
2. Once stock is reserved, order asks integration for a payment.
3. If the payment succeeds, the order is CONFIRMED and a shipment is created. It then moves to SHIPPED and DELIVERED.
4. If any step fails, the order is cancelled: reserved stock is released and a captured payment is refunded.

## Roles

- Anyone who registers through Keycloak becomes a **CUSTOMER**.
- `admin1` is the seeded shop **ADMIN**. They can make other users admins in the Keycloak console: `ecom-realm` → Users → user → Role mapping → assign `ADMIN`.
- Locally, `admin1` uses the dev password from the realm file. On GKE, the deploy replaces it with `ADMIN1_PASSWORD` from `k8s/secrets.env`.
- Brute-force protection is on: after 5 failed logins an account is temporarily locked, and the wait grows up to 15 minutes.

## Run locally

The full stack needs a lot of RAM, so start only what you need.

```bash
cp .env.example .env
```

Fill in Kafka (Confluent) and Cloudinary in `.env`, then:

```bash
docker compose up -d
```

This starts the base services: Postgres on 5433, Redis on 6380 and Keycloak on 8080. Add services by profile:

```bash
docker compose --profile catalog --profile search up -d
```

Profiles: `customer`, `catalog`, `inventory`, `order`, `integration`, `search`, `shop` (everything except customer), `proxy` (nginx on :8000 with Swagger at `/swagger`), `tracing` (Jaeger), and `all`.

Alternatively, run the base services in Docker and the Spring services from IntelliJ, with their env vars set in each run configuration.

Frontend:

```bash
cd frontend && npm install && npm run dev
```

`npm run dev:mock` runs the UI against Prism mocks generated from `openapi/`, with a fake login and no backend.

API docs for each service are at `/docs/<service>` behind the proxy.

## CI

GitHub Actions (`.github/workflows/ci.yml`):
- runs `mvn verify` for each service against a Postgres container;
- lints and builds the frontend;
- builds every Docker image (without pushing).

## Deploy (GKE)

See [k8s/README.md](k8s/README.md). Briefly:
1. Cloud Build builds the images (`cloudbuild.yaml`).
2. `k8s/deploy.sh` renders the manifests, creates secrets from the gitignored `k8s/secrets.env`, applies everything and resets `admin1`'s password.

Postgres runs in the cluster on a persistent volume, so its data survives redeploys and pod restarts.
