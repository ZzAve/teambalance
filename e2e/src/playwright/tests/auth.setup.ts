import { expect, test as setup } from "@playwright/test";
import path from "path";
import { HOST, PASSWORD } from "./utils";

const authFile = path.join(__dirname, "../../../.auth/user.json");
setup("authenticate", async ({ page }) => {
  await page.goto(HOST);
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
