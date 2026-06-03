/**
 * Global setup: wait for the backend to be fully ready before any tests run.
 *
 * Defense-in-depth on top of the Docker healthcheck — ensures the backend
 * is actually serving authenticated API routes before Playwright dispatches
 * the first test, even if the container scheduler starts the e2e container
 * slightly early.
 *
 * Two-stage readiness check:
 *   1. Actuator health: /internal/actuator/health (Spring Boot readiness)
 *   2. API probe: /api/users (authenticated application route) — closes the
 *      gap where actuator reports healthy but the Spring Security filter chain
 *      and schema initialization are still completing.
 *
 * In the Docker Compose network the backend is reachable as http://backend:8080.
 */

const BACKEND_HOST = `http://${process.env.VITE_SERVER_BACKEND ?? "backend"}:8080`;
const BACKEND_HEALTH_URL = `${BACKEND_HOST}/internal/actuator/health`;
const BACKEND_API_URL = `${BACKEND_HOST}/api/users`;
const TIMEOUT_MS = 120_000;
const POLL_INTERVAL_MS = 2_000;

async function pollUntilOk(
  url: string,
  credentials: string,
  deadline: number,
  label: string,
): Promise<void> {
  console.log(`[global-setup] Waiting for ${label} at ${url} …`);

  while (Date.now() < deadline) {
    try {
      const res = await fetch(url, {
        headers: { Authorization: `Basic ${credentials}` },
        signal: AbortSignal.timeout(3_000),
      });
      if (res.ok) {
        console.log(`[global-setup] ${label} ready (HTTP ${res.status})`);
        return;
      }
      console.log(
        `[global-setup] ${label} returned HTTP ${res.status}, retrying…`,
      );
    } catch (err) {
      console.log(
        `[global-setup] ${label} not reachable yet (${(err as Error).message}), retrying…`,
      );
    }
    await new Promise((r) => setTimeout(r, POLL_INTERVAL_MS));
  }

  throw new Error(
    `[global-setup] ${label} did not become ready within ${TIMEOUT_MS / 1000}s`,
  );
}

async function waitForBackend(): Promise<void> {
  const deadline = Date.now() + TIMEOUT_MS;
  const credentials = Buffer.from("admin:admin").toString("base64");

  // Stage 1: actuator health (fast Spring Boot readiness indicator)
  await pollUntilOk(
    BACKEND_HEALTH_URL,
    credentials,
    deadline,
    "backend actuator",
  );

  // Stage 2: authenticated API route — confirms the Spring Security filter
  // chain and tenant schema are fully initialised and serving API responses.
  await pollUntilOk(
    BACKEND_API_URL,
    credentials,
    deadline,
    "backend API (/api/users)",
  );
}

export default waitForBackend;
