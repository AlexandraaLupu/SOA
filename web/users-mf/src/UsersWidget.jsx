import React, { useEffect, useState } from "react";
import styles from "./styles.css?inline";

const STYLE_ID = "users-mf-styles";

function ensureStyles() {
  if (document.getElementById(STYLE_ID)) {
    return;
  }
  const styleTag = document.createElement("style");
  styleTag.id = STYLE_ID;
  styleTag.textContent = styles;
  document.head.appendChild(styleTag);
}

export default function UsersWidget({ apiBase, token }) {
  const [users, setUsers] = useState([]);
  const storedToken =
    token ||
    import.meta.env.VITE_ACCESS_TOKEN ||
    localStorage.getItem("accessToken");
  const authHeaders = storedToken ? { Authorization: `Bearer ${storedToken}` } : {};

  const loadUsers = async () => {
    const response = await fetch(`${apiBase}/users`, {
      headers: {
        "Content-Type": "application/json",
        ...authHeaders
      }
    });
    if (response.ok) {
      setUsers(await response.json());
    }
  };

  useEffect(() => {
    ensureStyles();
    loadUsers();
  }, []);

  return (
    <ul className="users">
      {users.map((user) => (
        <li key={user.id} className="users__row">
          <div>
            <p className="users__name">{user.name}</p>
            <p className="users__meta">{user.tier}</p>
          </div>
          <span className="users__id">#{user.id}</span>
        </li>
      ))}
    </ul>
  );
}
