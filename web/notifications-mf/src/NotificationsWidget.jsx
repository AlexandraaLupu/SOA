import React, { useEffect, useState } from "react";
import styles from "./styles.css?inline";

const STYLE_ID = "notifications-mf-styles";

function ensureStyles() {
  if (document.getElementById(STYLE_ID)) {
    return;
  }
  const styleTag = document.createElement("style");
  styleTag.id = STYLE_ID;
  styleTag.textContent = styles;
  document.head.appendChild(styleTag);
}
import { Client } from "@stomp/stompjs";

export default function NotificationsWidget({ wsBase }) {
  const [events, setEvents] = useState([]);
  const [status, setStatus] = useState("connecting");

  const parseEvent = (payload) => {
    try {
      const parsed = JSON.parse(payload);
      return {
        raw: payload,
        orderId: parsed.orderId,
        userId: parsed.userId,
        tableNumber: parsed.tableNumber,
        item: parsed.item,
        createdAt: parsed.createdAt
      };
    } catch {
      return { raw: payload };
    }
  };

  useEffect(() => {
    ensureStyles();
    const client = new Client({
      brokerURL: `${wsBase}`,
      reconnectDelay: 5000,
      onConnect: () => {
        setStatus("connected");
        client.subscribe("/topic/notifications", (message) => {
          setEvents((prev) => [message.body, ...prev].slice(0, 6));
        });
      },
      onStompError: () => {
        setStatus("error");
      }
    });

    client.activate();
    return () => client.deactivate();
  }, [wsBase]);

  return (
    <div className="notifications">
      <p className={`notifications__status notifications__status--${status}`}>
        {status}
      </p>
      <ul>
        {events.map((event, index) => {
          const parsed = parseEvent(event);
          return (
            <li key={`${event}-${index}`} className="notifications__row">
              <div>
                <p className="notifications__item">
                  {parsed.item || "Activity update"}
                </p>
                <p className="notifications__meta">
                  Order {parsed.orderId ?? "—"} · Table {parsed.tableNumber ?? "—"} · User {parsed.userId ?? "—"}
                </p>
              </div>
              <span className="notifications__time">
                {parsed.createdAt
                  ? new Date(parsed.createdAt).toLocaleTimeString()
                  : "now"}
              </span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
