import { expect, test } from "@playwright/test";
import { HOST, START_OF_SEASON } from "./utils";
import { createTrainingEvent } from "./training-utils";
import { v4 as uuid } from "uuid";

test("Validate previous events can be shown", async ({ page }) => {
  await page.goto(HOST);
  await page.getByText("Aanstaande trainingen").waitFor({ state: "visible" });
  await page.getByRole("button", { name: "Admin dingen" }).click();

  // Use a unique comment so we can find THIS specific past event later,
  // even when other tests have created training events in the database.
  const comment = `past-${uuid().slice(0, 8)}`;
  await createTrainingEvent(page, comment, START_OF_SEASON);
  await page.getByRole("button", { name: "Terug naar de veiligheid" }).click();

  await page.getByTestId("training-events").waitFor({ state: "visible" });
  await page
    .getByTestId("training-events")
    .first()
    .getByTestId("more-button")
    .first()
    .click();

  await page.getByTestId("event-list").waitFor({ state: "visible" });

  const oudeEvents = page.getByLabel("Oude events");

  // Ensure the filter starts OFF — the checkbox may already be checked from
  // a previous run or because it is the default state in this view.
  await oudeEvents.uncheck();
  await page.getByTestId("event-list").waitFor({ state: "visible" });

  // The past event must NOT appear while old events are hidden.
  await expect(
    page.getByTestId("event-list-item").filter({ hasText: comment }),
  ).not.toBeVisible();

  // Toggle old events ON.
  await oudeEvents.check();
  await page.getByTestId("event-list").waitFor({ state: "visible" });

  // The past event must now be visible.
  await expect(
    page.getByTestId("event-list-item").filter({ hasText: comment }),
  ).toBeVisible();
});
