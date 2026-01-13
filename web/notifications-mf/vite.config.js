import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import federation from "@originjs/vite-plugin-federation";

export default defineConfig({
  server: {
    port: 5173,
    strictPort: true,
    host: "0.0.0.0",
    cors: true,
    headers: {
      "Access-Control-Allow-Origin": "*"
    }
  },
  plugins: [
    react(),
    federation({
      name: "notifications",
      filename: "remoteEntry.js",
      exposes: {
        "./NotificationsWidget": "./src/NotificationsWidget.jsx"
      },
      shared: ["react", "react-dom", "@stomp/stompjs"]
    })
  ],
  build: {
    target: "esnext"
  }
});
