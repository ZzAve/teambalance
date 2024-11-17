import react from "@vitejs/plugin-react";

import { defineConfig, loadEnv } from "vite";
import { visualizer } from "rollup-plugin-visualizer";

export default defineConfig(({ mode }) => {
  const root = "src/main/react";
  console.log("Running in " + mode + " mode");
  const env = loadEnv(mode, process.cwd() + "/" + root, "VITE");
  console.log("All env vars:", env);

  return {
    root: root,
    build: {
      // Relative to the root
      outDir: env.VITE_BUILD_OUT_DIR || "../../../target/classes/static",
      manifest: true,
      sourcemap: "hidden",
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
        "/api": `http://${env.VITE_SERVER_BACKEND || "localhost"}:8080`,
      },
    },
    preview: {
      port: 3001,
    },
  };
});
