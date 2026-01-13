import React from "react";
import { createRoot } from "react-dom/client";
import NotificationsWidget from "./NotificationsWidget.jsx";
import "./styles.css";

createRoot(document.getElementById("root")).render(
  <NotificationsWidget wsBase={import.meta.env.VITE_WS_BASE || "ws://localhost/ws"} />
);
