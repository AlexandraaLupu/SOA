import React, { Suspense, useEffect, useState } from "react";
import Keycloak from "keycloak-js";

const OrdersWidget = React.lazy(() => import("orders/OrdersWidget"));
const UsersWidget = React.lazy(() => import("users/UsersWidget"));
const NotificationsWidget = React.lazy(
  () => import("notifications/NotificationsWidget")
);

const apiBase = import.meta.env.VITE_API_BASE || "http://localhost/api";
const wsBase =
  import.meta.env.VITE_WS_BASE || "ws://localhost/ws";

const keycloak = new Keycloak({
  url:
    import.meta.env.VITE_KEYCLOAK_URL ||
    `${window.location.origin}/keycloak`,
  realm: import.meta.env.VITE_KEYCLOAK_REALM || "demo",
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "demo-client"
});

export default function App() {
  const [authReady, setAuthReady] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [token, setToken] = useState(null);

  useEffect(() => {
    let refreshTimer;
    keycloak
      .init({
        onLoad: "login-required",
        pkceMethod: "S256",
        checkLoginIframe: false
      })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        if (authenticated) {
          localStorage.setItem("accessToken", keycloak.token);
          setToken(keycloak.token);
          refreshTimer = setInterval(() => {
            keycloak
              .updateToken(30)
              .then((refreshed) => {
                if (refreshed && keycloak.token) {
                  localStorage.setItem("accessToken", keycloak.token);
                  setToken(keycloak.token);
                }
              })
              .catch(() => {
                keycloak.login();
              });
          }, 10000);
        }
        setAuthReady(true);
      })
      .catch(() => {
        setAuthReady(true);
      });

    return () => {
      if (refreshTimer) {
        clearInterval(refreshTimer);
      }
    };
  }, []);

  if (!authReady) {
    return <p>Connecting to Keycloak...</p>;
  }
  if (!isAuthenticated || !token) {
    return <p>Waiting for authentication...</p>;
  }

  return (
    <div className="shell">
      <header className="shell__top">
        <div className="shell__brand">
          <span className="shell__kicker">Event Ticketing</span>
          <h1>Ticket Flow Console</h1>
          <p>
            Secured gateways, event streaming, and real-time ticket updates
            across the platform.
          </p>
        </div>
        <div className="shell__auth">
          <div className="shell__auth-state">
            <span className="shell__dot" />
            {isAuthenticated ? "Authenticated" : "Anonymous"}
          </div>
          <button
            type="button"
            onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
          >
            Logout
          </button>
        </div>
      </header>

      <main className="shell__layout">
        <section className="panel panel--orders">
          <div className="panel__head">
            <h2>Orders</h2>
            <span>Gateway API</span>
          </div>
          <Suspense fallback={<p className="panel__loading">Loading orders...</p>}>
            <OrdersWidget apiBase={apiBase} token={token} />
          </Suspense>
        </section>
        <section className="panel panel--users">
          <div className="panel__head">
            <h2>Users</h2>
            <span>Profiles + tiers</span>
          </div>
          <Suspense fallback={<p className="panel__loading">Loading users...</p>}>
            <UsersWidget apiBase={apiBase} token={token} />
          </Suspense>
        </section>
        <section className="panel panel--notifications">
          <div className="panel__head">
            <h2>Notifications</h2>
            <span>WS fan-out</span>
          </div>
          <Suspense fallback={<p className="panel__loading">Connecting...</p>}>
            <NotificationsWidget wsBase={wsBase} />
          </Suspense>
        </section>
      </main>
    </div>
  );
}
