import react from "@vitejs/plugin-react";
import path from "path";
import { fileURLToPath } from "node:url";

import { defineConfig, loadEnv } from "vite";
import { visualizer } from "rollup-plugin-visualizer";

export default defineConfig(({ mode }) => {
  // Resolve the ESM subtree of @mui/icons-material.
  // We can't use require.resolve("@mui/icons-material/package.json") because
  // the package's exports map does not include the "package.json" subpath.
  // import.meta.resolve resolves to the ESM entry (esm/index.js), so dirname
  // gives us the esm/ directory directly — use that as the alias target.
  const muiIconsEsm = path.dirname(
    fileURLToPath(import.meta.resolve("@mui/icons-material")),
  );

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
    resolve: {
      alias: {
        // @mui/icons-material ships both CJS (default) and ESM (esm/) builds.
        // Vite 8 (Rolldown) does not apply __esModule interop the same way
        // esbuild does, so default imports from the CJS paths resolve to the
        // module namespace object instead of the component — crashing React.
        // Point the alias at the pre-built ESM subtree to avoid the issue.
        "@mui/icons-material": muiIconsEsm,
      },
    },
    plugins: [
      visualizer(),
      react({
        // Use React plugin in all *.jsx and *.tsx files
        include: "**/*.{jsx,tsx}",
      }),
    ],
    resolve: {
      alias: [
        // Vite 8 no longer auto-interops CJS deep imports; redirect MUI icon
        // deep imports to their ESM equivalents to avoid "got: object" errors.
        {
          find: /^@mui\/icons-material\/([^/]+)$/,
          replacement: "@mui/icons-material/esm/$1",
        },
      ],
    },
    server: {
      host: "0.0.0.0",
      port: 3000,
      allowedHosts: ["localhost", "127.0.0.1", "frontend"],
      proxy: {
        "/api": `http://${env.VITE_SERVER_BACKEND || "localhost"}:8080`,
      },
      warmup: {
        clientFiles: [
          `./index.tsx`,
          `./src/App.tsx`,
        ],
      },
    },
    preview: {
      host: "0.0.0.0",
      port: 3000,
      proxy: {
        "/api": `http://${env.VITE_SERVER_BACKEND || "localhost"}:8080`,
      },
    },
  };
});