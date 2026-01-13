import React from "react";
import { createRoot } from "react-dom/client";
import OrdersWidget from "./OrdersWidget.jsx";
import "./styles.css";

createRoot(document.getElementById("root")).render(
  <OrdersWidget apiBase={import.meta.env.VITE_API_BASE || "http://localhost/api"} />
);
