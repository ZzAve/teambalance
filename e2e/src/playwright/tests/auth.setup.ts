import { expect, test as setup } from "@playwright/test";
import path from "path";
import { HOST, PASSWORD } from "./utils";

const authFile = path.join(__dirname, "../../../.auth/user.json");
// Give auth setup extra time: even after the healthcheck warms up the Vite entry
// point, the browser still needs to fetch and compile the remaining module graph
// on first load, which can take a while in Docker.
setup.setTimeout(120000);
setup("authenticate", async ({ page }) => {
  await page.goto(HOST);
  await page.getByPlaceholder("******").waitFor({ state: "visible" });
  await page.getByPlaceholder("******").click();
  await page.getByPlaceholder("******").fill(PASSWORD);
  await page.getByPlaceholder("******").press("Enter");
  await expect(page.locator("#root")).toContainText("Aanstaande trainingen");
  await expect(page.locator("#root")).toContainText("Aanstaande wedstrijden");
  await expect(page.locator("#root")).toContainText(
    "Aanstaande andere evenementen en uitjes",
  );
  await expect(page.locator("#root")).toContainText("De bierstand");
  await expect(page.locator("#root")).toContainText("Transacties");

  // End of authentication steps.
  console.log(`Storing authFile in ${authFile}`);
  await page.context().storageState({ path: authFile });
});
