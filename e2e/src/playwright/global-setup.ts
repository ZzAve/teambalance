/**
 * Global setup: wait for the backend health endpoint before any tests run.
 *
 * Defense-in-depth on top of the Docker healthcheck — ensures the backend
 * is actually serving before Playwright dispatches the first test, even if
 * the container scheduler starts the e2e container slightly early.
 *
 * The backend actuator path is /internal/actuator/health (auth required).
 * In the Docker Compose network the backend is reachable as http://backend:8080.
 */

const BACKEND_HEALTH_URL = `http://${process.env.VITE_SERVER_BACKEND ?? "backend"}:8080/internal/actuator/health`;
const TIMEOUT_MS = 120_000;
const POLL_INTERVAL_MS = 2_000;

async function waitForBackend(): Promise<void> {
  const deadline = Date.now() + TIMEOUT_MS;
  const credentials = Buffer.from("admin:admin").toString("base64");

  console.log(`[global-setup] Waiting for backend at ${BACKEND_HEALTH_URL} …`);

  while (Date.now() < deadline) {
    try {
      const res = await fetch(BACKEND_HEALTH_URL, {
        headers: { Authorization: `Basic ${credentials}` },
        signal: AbortSignal.timeout(3_000),
      });
      if (res.ok) {
        console.log(`[global-setup] Backend is healthy (HTTP ${res.status})`);
        return;
      }
      console.log(
        `[global-setup] Backend returned HTTP ${res.status}, retrying…`,
      );
    } catch (err) {
      // connection refused / timeout — backend not up yet
      console.log(
        `[global-setup] Backend not reachable yet (${(err as Error).message}), retrying…`,
      );
    }
    await new Promise((r) => setTimeout(r, POLL_INTERVAL_MS));
  }

  throw new Error(
    `[global-setup] Backend did not become healthy within ${TIMEOUT_MS / 1000}s`,
  );
}

export default waitForBackend;
