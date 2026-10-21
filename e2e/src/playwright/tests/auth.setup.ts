import { expect, test as setup } from "@playwright/test";
import path from "path";
import { HOST, PASSWORD } from "./utils";

const authFile = path.join(__dirname, "../../../.auth/user.json");
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

  // One-time page-readiness check: reload using the stored auth state and
  // confirm the Overview renders.  This closes the race where the auth file
  // is written while the backend is still warming up — if the page fails to
  // render here, the setup step fails fast rather than every test timing out.
  await page.goto(HOST);
  await page
    .getByText("Aanstaande trainingen")
    .waitFor({ state: "visible", timeout: 90_000 });
  console.log("[auth-setup] Overview page rendered with stored auth state ✓");
});
