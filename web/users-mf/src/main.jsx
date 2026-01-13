import React from "react";
import { createRoot } from "react-dom/client";
import UsersWidget from "./UsersWidget.jsx";
import "./styles.css";

createRoot(document.getElementById("root")).render(
  <UsersWidget apiBase={import.meta.env.VITE_API_BASE || "http://localhost/api"} />
);
