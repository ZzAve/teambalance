import { expect, test } from "@playwright/test";
import { HOST } from "./utils";

test.describe("login/logout functionality", () => {
  test.beforeEach(async ({ page }) => {
    // Go to the starting url before each test.
    await page.goto(HOST);
  });

  test("Login", async ({ page }) => {
    await expect(page.locator("#root")).toContainText("Transacties");
  });

  test("Logout", async ({ page }) => {
    // login
    await expect(page.getByRole("button", { name: "Logout" })).toBeVisible();
    await page.getByRole("button", { name: "Logout" }).click();
    await expect(
      page.locator("span").filter({ hasText: "Login" }),
    ).toBeVisible();
    await expect(page.getByRole("button", { name: "Login" })).toBeVisible();
  });
});
