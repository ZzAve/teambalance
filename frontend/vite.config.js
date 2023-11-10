import react from "@vitejs/plugin-react";

import { defineConfig } from "vite";
import { visualizer } from "rollup-plugin-visualizer";

export default defineConfig({
  root: "src/main/react",
  build: {
    // Relative to the root
    outDir: "../../../target/classes/static",
    emptyOutDir: true,
  },
  plugins: [
    visualizer(),
    react({
      // Use React plugin in all *.jsx and *.tsx files
      include: "**/*.{jsx,tsx}",
    }),
  ],
  server: {
    host: "0.0.0.0",
    port: 3000,
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
  preview: {
    port:3001,
  }
});
