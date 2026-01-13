# Tutorial: Securing a REST API with Keycloak + Spring Boot Gateway

This tutorial explains how the project secures a REST API using a Spring Boot
gateway and Keycloak. It walks through the architecture, configuration,
token flow, and concrete request examples taken from the project. The setup is
fully functional and can be run locally with Docker Compose.

---

## 1) What we are securing

The system exposes a REST API through a **Gateway** service. Clients (the
micro‑frontend shell) never call backend services directly. Instead, they call
`/api/...` on the gateway, and the gateway forwards requests to internal
services:

- `/api/orders` → Orders Service
- `/api/users` → Users Service

Security goals:

1) **Authenticate** every request (users must log in).
2) **Validate** the token centrally at the gateway.
3) **Hide** internal services behind the gateway.

This approach keeps security logic in one place and avoids duplicating
authentication checks in each service.

---

## 2) Architecture overview

Key components:

- **Keycloak**: identity provider (OAuth2/OIDC).
- **Gateway**: Spring Boot resource server that validates JWTs.
- **Web Shell (Micro‑frontend host)**: obtains tokens and attaches them to API
  requests.
- **Orders / Users Services**: downstream services with no public exposure.

Traffic flow:

1) The user opens the web shell.
2) The shell redirects to Keycloak login.
3) Keycloak returns an access token (JWT).
4) The shell includes `Authorization: Bearer <token>` when calling `/api/*`.
5) The gateway validates the JWT and forwards requests downstream.

---

## 3) Keycloak configuration

Keycloak runs in Docker Compose with a realm import:

**File:** `deploy/keycloak/realm-export.json`

Key points:

- Realm: `demo`
- Client: `demo-client`
  - Public client
  - Redirect URIs: `http://localhost/*`
  - Web origins: `http://localhost`
- Users: `user1`, `user2`, `user3` (password: `demo`)


---

## 4) Gateway security (Spring Boot)

The gateway is configured as an OAuth2 **resource server**. This means:

- It accepts incoming REST calls.
- It validates JWTs from Keycloak.
- It only allows authenticated requests.

Relevant files:

- `services/gateway/src/main/java/org/example/gateway/GatewaySecurityConfig.java`
- `services/gateway/src/main/java/org/example/gateway/GatewayController.java`
- `services/gateway/src/main/resources/application.yml`

In the gateway’s security config, the JWT issuer and JWK set are wired to
Keycloak. The resource server verifies:

- Token signature
- Token expiration
- Issuer match

Gateway configuration in Docker Compose (key excerpts):

- The gateway is started with JWT settings:
  - `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`
  - `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI`
- These point to the Keycloak realm inside the Docker network.

This ensures the gateway uses Keycloak to validate tokens while the downstream
services never need to know about auth.

---

## 5) Web shell authentication (Keycloak JS)

The micro‑frontend shell uses the **Keycloak JS** SDK to:

1) Redirect the user to Keycloak login.
2) Receive a JWT access token.
3) Store the token and attach it to API calls.

Relevant file:

- `web/shell/src/App.jsx`

The shell initializes Keycloak with:

- `url`: `http://localhost/keycloak`
- `realm`: `demo`
- `clientId`: `demo-client`

When authenticated, it stores the token in `localStorage` and passes it to the
Orders/Users widgets so they can send authenticated requests.

---

## 6) Example: authenticated Orders request

Once logged in, the Orders widget sends:

```
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "tableNumber": 12,
  "item": "Margherita + Cola"
}
```

The gateway validates the token, then proxies the request to the Orders service.
If the token is missing or invalid, the gateway returns **401 Unauthorized**.

---

## 7) Getting a token via curl (manual test)

If you want to test without the browser, you can use the password grant against
Keycloak’s token endpoint.

```
curl -X POST \
  "http://localhost/keycloak/realms/demo/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=demo-client" \
  -d "username=user1" \
  -d "password=demo"
```

This returns a JSON response with `access_token`. Then call the gateway:

```
curl -X POST http://localhost/api/orders \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"tableNumber":12,"item":"Margherita + Cola"}'
```

You should receive a `201` response with the order payload.

---

## 8) Why security lives at the gateway

Placing security at the gateway gives:

- **Single policy point**: no duplicated auth logic.
- **Simpler microservices**: orders/users focus on business logic only.
- **Consistent error handling**: all unauthorized requests fail in one place.

This pattern scales well as you add more services.

---

## 9) Running the secured stack locally

1) Build services:
```
./gradlew :services:gateway:bootJar :services:orders:bootJar :services:users:bootJar \
  :services:notifications:bootJar :services:worker:bootJar
```

2) Start containers:
```
docker compose -f deploy/docker-compose.yml up --build
```

3) Open the shell:
```
http://localhost:5170
```

4) Log in with:
```
user1 / demo
```
---
