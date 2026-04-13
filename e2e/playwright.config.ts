import { defineConfig, devices } from "@playwright/test";

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
import dotenv from "dotenv";
import path from "path";

dotenv.config({ quiet: true, path: path.resolve(__dirname, ".env") });

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: "./src/playwright/tests",
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 1 : 1,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Auth restoration via localStorage involves recursive API retries (up to
     10x, ~5 s each). On CI with a slow backend this can take 20-30 s before
     the app renders authenticated content. Both timeouts are raised so tests
     don't race against the startup auth flow. */
  timeout: 30000,
  expect: { timeout: 10000 },
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [
    ["html", { open: "never" }],
    [process.env.CI ? "github" : "list"],
    ["json", { outputFile: "results.json" }],
  ],
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    // baseURL: 'http://127.0.0.1:3000',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: "retain-on-failure",
    screenshot: {
      mode: "only-on-failure"
    }
  },

  /* Configure projects for major browsers.
   * On CI only chromium runs to keep the pipeline fast (2 vCPU runner, serial
   * workers). Firefox and webkit are available for local cross-browser checks. */
  projects: [
    // Setup project
    { name: "setup", testMatch: /.*\.setup\.ts/ },

    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
        storageState: ".auth/user.json",
      },
      dependencies: ["setup"],
    },

    ...(!process.env.CI ? [
      {
        name: "firefox",
        use: {
          ...devices["Desktop Firefox"],
          storageState: ".auth/user.json",
        },
        dependencies: ["setup"],
      },

      {
        name: "webkit",
        use: {
          ...devices["Desktop Safari"],
          storageState: ".auth/user.json",
        },
        dependencies: ["setup"],
      },
    ] : []),

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ],

  /* Run your local dev server before starting the tests */
  // webServer: {
  //   command: "cd .. && make run-local",
  //   url: "http://127.0.0.1:8080",
  //   reuseExistingServer: !process.env.CI,
  //   stdout: "pipe",
  // },
});
