import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import federation from "@originjs/vite-plugin-federation";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const ordersRemote =
    env.VITE_REMOTE_ORDERS ||
    "http://localhost:5171/assets/remoteEntry.js";
  const usersRemote =
    env.VITE_REMOTE_USERS ||
    "http://localhost:5172/assets/remoteEntry.js";
  const notificationsRemote =
    env.VITE_REMOTE_NOTIFICATIONS ||
    "http://localhost:5173/assets/remoteEntry.js";

  return {
    server: {
      port: 5170,
      strictPort: true,
      proxy: {
        "/keycloak": {
          target: "http://localhost",
          changeOrigin: true
        }
      }
    },
    plugins: [
      react(),
      federation({
        name: "shell",
        remotes: {
          orders: ordersRemote,
          users: usersRemote,
          notifications: notificationsRemote
        },
        shared: ["react", "react-dom"]
      })
    ],
    build: {
      target: "esnext"
    }
  };
});
