import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import federation from "@originjs/vite-plugin-federation";

export default defineConfig({
  server: {
    port: 5172,
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
      name: "users",
      filename: "remoteEntry.js",
      exposes: {
        "./UsersWidget": "./src/UsersWidget.jsx"
      },
      shared: ["react", "react-dom"]
    })
  ],
  build: {
    target: "esnext"
  }
});
