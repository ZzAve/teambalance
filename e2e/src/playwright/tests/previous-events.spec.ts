import { expect, test } from "@playwright/test";
import { HOST, START_OF_SEASON } from "./utils";
import { createTrainingEvent } from "./training-utils";

test("Validate previous events can be shown", async ({ page }) => {
  await page.goto(HOST);
  await page.getByRole("button", { name: "Admin dingen" }).click();
  await createTrainingEvent(page, "test", START_OF_SEASON);
  await page.getByRole("button", { name: "Terug naar de veiligheid" }).click();

  await page.getByTestId("training-events").waitFor({ state: "visible" });
  await page
    .getByTestId("training-events")
    .first()
    .getByTestId("more-button")
    .first()
    .click();

  await page.getByTestId("event-list").waitFor({ state: "visible" });
  const eventListBefore = await page
    .getByTestId("event-list-item")
    .allInnerTexts();

  await page.getByLabel("Oude events").check();

  await page.getByTestId("event-list").waitFor({ state: "visible" });
  const eventListAfter = await page
    .getByTestId("event-list-item")
    .allInnerTexts();

  expect(eventListAfter).not.toBeFalsy();
  expect(eventListBefore).not.toEqual(eventListAfter);
});
