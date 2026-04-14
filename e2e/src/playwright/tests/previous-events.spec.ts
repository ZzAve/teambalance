import { expect, test } from "@playwright/test";
import { getStartOfSeason, HOST } from "./utils";
import { createTrainingEvent } from "./training-utils";
import { v4 as uuid } from "uuid";

test("Validate previous events can be shown", async ({ page, request }) => {
  await page.goto(HOST);
  await page.getByText("Aanstaande trainingen").waitFor({ state: "visible" });
  await page.getByRole("button", { name: "Admin dingen" }).click();

  // Use a unique comment so we can find THIS specific past event later,
  // even when other tests have created training events in the database.
  const comment = `past-${uuid().slice(0, 8)}`;

  const dayBeforeStartOfSeason = await getStartOfSeason(request);
  dayBeforeStartOfSeason.setDate(dayBeforeStartOfSeason.getDate() + 2);
  await createTrainingEvent(page, comment, dayBeforeStartOfSeason);
  await page.getByRole("button", { name: "Terug naar de veiligheid" }).click();

  const trainingEvents = page.getByTestId("training-events");
  await trainingEvents.waitFor({ state: "visible" });
  await trainingEvents.first().getByTestId("more-button").first().click();

  // more-button navigates to the /trainings route — wait for that before
  // checking event-list (otherwise we'd still be on Overview with 3 lists).
  await page.waitForURL("**/trainings");
  const eventList = page.getByTestId("event-list");
  await eventList.waitFor({ state: "visible" });

  const oudeEvents = page.getByLabel("Oude events");

  // Ensure the filter starts OFF — the checkbox may already be checked from
  // a previous run or because it is the default state in this view.
  await oudeEvents.uncheck();
  await eventList.waitFor({ state: "visible" });

  // The past event must NOT appear while old events are hidden.
  await expect(
    page.getByTestId("event-list-item").filter({ hasText: comment }),
  ).not.toBeVisible();

  // Toggle old events ON.
  await oudeEvents.check();
  await eventList.waitFor({ state: "visible" });

  // The past event must now be visible.
  await expect(
    page.getByTestId("event-list-item").filter({ hasText: comment }),
  ).toBeVisible();
});
