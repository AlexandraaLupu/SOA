import React, { useEffect, useState } from "react";
import styles from "./styles.css?inline";

const STYLE_ID = "orders-mf-styles";

function ensureStyles() {
  if (document.getElementById(STYLE_ID)) {
    return;
  }
  const styleTag = document.createElement("style");
  styleTag.id = STYLE_ID;
  styleTag.textContent = styles;
  document.head.appendChild(styleTag);
}

export default function OrdersWidget({ apiBase, token }) {
  const [orders, setOrders] = useState([]);
  const [item, setItem] = useState("");
  const [tableNumber, setTableNumber] = useState("1");
  const [userId, setUserId] = useState(null);
  const storedToken =
    token ||
    import.meta.env.VITE_ACCESS_TOKEN ||
    localStorage.getItem("accessToken");
  const authHeaders = storedToken ? { Authorization: `Bearer ${storedToken}` } : {};

  const parseUsername = () => {
    if (!storedToken) {
      return null;
    }
    try {
      const payload = storedToken.split(".")[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
      return decoded.preferred_username || decoded.username || null;
    } catch {
      return null;
    }
  };

  const loadOrders = async () => {
    const response = await fetch(`${apiBase}/orders`, {
      headers: {
        "Content-Type": "application/json",
        ...authHeaders
      }
    });
    if (response.ok) {
      setOrders(await response.json());
    }
  };

  useEffect(() => {
    ensureStyles();
    loadOrders();
    const username = parseUsername();
    if (!username) {
      return;
    }
    fetch(`${apiBase}/users`, {
      headers: {
        "Content-Type": "application/json",
        ...authHeaders
      }
    })
      .then((response) => (response.ok ? response.json() : []))
      .then((users) => {
        const match = users.find((user) => user.username === username);
        if (match) {
          setUserId(match.id);
        }
      })
      .catch(() => {});
  }, []);

  const submitOrder = async (event) => {
    event.preventDefault();
    await fetch(`${apiBase}/orders`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...authHeaders
      },
      body: JSON.stringify({
        userId: userId ?? 0,
        tableNumber: Number(tableNumber),
        item
      })
    });
    setItem("");
    setTableNumber("1");
    await loadOrders();
  };

  return (
    <div className="orders">
      <form className="orders__form" onSubmit={submitOrder}>
        <input
          value={tableNumber}
          onChange={(event) => setTableNumber(event.target.value)}
          placeholder="Table #"
        />
        <input
          value={item}
          onChange={(event) => setItem(event.target.value)}
          placeholder="Order item"
        />
        <button type="submit" disabled={userId == null}>
          {userId == null ? "Waiting for user" : "Create"}
        </button>
      </form>
      <ul className="orders__list">
        {orders.map((entry, index) => (
          <li key={entry.order?.id ?? index} className="orders__row">
            <div>
              <p className="orders__item">{entry.order?.item}</p>
              <p className="orders__meta">
                Table {entry.order?.tableNumber} · {entry.user?.name} · {entry.order?.status}
              </p>
            </div>
            <span className="orders__id">#{entry.order?.id}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
