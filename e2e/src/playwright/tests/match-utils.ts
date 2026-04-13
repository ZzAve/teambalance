import { expect, APIRequestContext, Page, Locator } from "@playwright/test";
import { addDays, ensure, HOST, NOW, pickDateTime } from "./utils";

/**
 * The X-Secret header value for the local/e2e tenant (base64 of "teambalance").
 * Matches the secret configured in application-local.yml for domain frontend:3000.
 */
const API_SECRET = Buffer.from("teambalance").toString("base64");

/**
 * Headers required by the backend API: tenant resolution via Host + secret auth.
 * The Vite dev server proxies /api/* to backend:8080 and forwards the Host header.
 */
const apiHeaders = {
  Host: "frontend:3000",
  "X-Secret": API_SECRET,
  "Content-Type": "application/json",
};

/**
 * Create a team member user via the backend API.
 * Required so that newly created events have attendees to interact with.
 *
 * @returns the created user's id (for cleanup)
 */
export async function createUserViaApi(
  request: APIRequestContext,
  name: string,
  role: string = "OTHER",
): Promise<string> {
  const response = await request.post(`${HOST}/api/users`, {
    headers: apiHeaders,
    data: { name, role },
  });
  if (!response.ok()) {
    throw new Error(
      `Failed to create user "${name}": ${response.status()} ${await response.text()}`,
    );
  }
  const body = await response.json();
  return ensure(body.id as string | undefined, `user id for "${name}"`);
}

/**
 * Delete a team member user via the backend API (requires admin credentials).
 */
export async function deleteUserViaApi(
  request: APIRequestContext,
  userId: string,
): Promise<void> {
  const response = await request.delete(`${HOST}/api/users/${userId}`, {
    headers: {
      ...apiHeaders,
      // DELETE /api/users/{id} is @Admin-protected; use HTTP Basic auth.
      Authorization: `Basic ${Buffer.from("admin:admin").toString("base64")}`,
    },
  });
  if (!response.ok() && response.status() !== 404) {
    throw new Error(
      `Failed to delete user "${userId}": ${response.status()} ${await response.text()}`,
    );
  }
}

/**
 * Generate realistic match date (next Saturday at 14:00)
 */
export function getNextMatchDate(): Date {
  const date = new Date();
  const daysUntilSaturday = (6 - date.getDay() + 7) % 7;
  const matchDate = addDays(date, daysUntilSaturday || 7);
  matchDate.setHours(14, 0, 0, 0);
  return matchDate;
}

/**
 * Create a match event via admin UI
 */
export async function createMatchEvent(
  page: Page,
  opponent: string,
  options?: {
    date?: Date;
    location?: string;
    description?: string;
  },
): Promise<string> {
  const date = options?.date ?? addDays(NOW, 7); // Default: next week
  const location = options?.location ?? "Test Sporthal";
  const description = options?.description;

  // Navigate to the Wedstrijden section (admin page defaults to Trainingen)
  await page.getByRole("button", { name: /wedstrijden/i }).click();

  // Click create button
  await page.getByRole("button", { name: /nieuwe wedstrijd/i }).click();

  // Fill opponent field (required for matches)
  await page.getByLabel("Tegenstander *").fill(opponent);

  // Use shared date picker
  await pickDateTime(page, date);

  // Fill location
  await page.getByLabel("Locatie *").fill(location);

  // Fill description if provided
  if (description) {
    await page.getByLabel("Opmerking").fill(description);
  }

  // Select audience (default: everyone)
  await page.getByLabel("Iedereen").check();

  // Submit
  await page.getByRole("button", { name: "Opslaan" }).click();

  // Wait for success toast and extract event ID.
  // eventType() returns "Wedstrijd" for matches, so the message is "Wedstrijd event (id ...)".
  await expect(page.getByRole("alert")).toContainText("Wedstrijd event");
  const snackbarText = await page.getByRole("alert").textContent();
  const matches = snackbarText?.match(/id ([a-f0-9-]+)/);
  const eventId = matches && matches[1];

  // Dismiss toast (may auto-hide; ignore errors)
  await page
    .getByRole("alert")
    .getByRole("button")
    .click({ timeout: 3000 })
    .catch(() => {});
  await page
    .getByRole("alert")
    .waitFor({ state: "hidden", timeout: 10000 })
    .catch(() => {});

  return ensure(eventId, "match event ID");
}

/**
 * Update match details
 */
export async function updateMatch(
  page: Page,
  eventId: string,
  newOpponent?: string,
  newLocation?: string,
): Promise<void> {
  await page
    .getByRole("button", { name: `Update event ${eventId}` })
    .click({ timeout: 5000 });

  if (newOpponent) {
    await page.getByLabel("Tegenstander *").fill(newOpponent);
  }

  if (newLocation) {
    await page.getByLabel("Locatie *").fill(newLocation);
  }

  await page.getByRole("button", { name: "Opslaan" }).click();

  await expect(page.getByRole("alert")).toContainText(
    `Wedstrijd event (id ${eventId}) geüpdate`,
  );

  await page
    .getByRole("alert")
    .getByRole("button")
    .click({ timeout: 3000 })
    .catch(() => {});
  await page
    .getByRole("alert")
    .waitFor({ state: "hidden", timeout: 10000 })
    .catch(() => {});
}

/**
 * Delete match event with confirmation
 */
export async function deleteMatch(page: Page, eventId: string): Promise<void> {
  // Navigate to the Wedstrijden section (admin page defaults to Trainingen)
  await page.getByRole("button", { name: /wedstrijden/i }).click();

  let combobox = page.getByRole("combobox", { name: "Rows per page:" });
  await combobox.isVisible();
  await combobox.click();
  await page.getByRole("option", { name: "50" }).click();

  await page
    .getByRole("button", { name: `Verwijder event ${eventId}` })
    .click();

  await expect(
    page.getByRole("heading", { name: "Weet je zeker" }),
  ).toContainText(
    `Weet je zeker dat je match met id #${eventId} wil verwijderen`,
  );

  await page.getByRole("button", { name: "OK" }).click();

  // Use filter so multiple concurrent snackbars don't cause a strict-mode violation.
  await expect(
    page
      .getByRole("alert")
      .filter({ hasText: `Event #${eventId} is verwijderd` }),
  ).toBeVisible();
}

/**
 * Map from logical attendance status to the MUI button color class suffix.
 * AttendeeButton uses color="success" for PRESENT, "error" for ABSENT,
 * "warning" for UNCERTAIN (maybe).
 */
const statusToMuiColorClass: Record<"attending" | "maybe" | "absent", string> =
  {
    attending: "MuiButton-colorSuccess",
    maybe: "MuiButton-colorWarning",
    absent: "MuiButton-colorError",
  };

/**
 * Set attendance status for a specific attendee in the event attendee list.
 *
 * Flow (matches actual UI):
 *  1. Click the attendee's name button to open the AttendeeRefinement view.
 *  2. Click the appropriate icon button (CheckIcon=PRESENT, ClearIcon=ABSENT,
 *     HelpIcon=UNCERTAIN).
 *  3. Wait for the refinement view to close — i.e. the attendee name button
 *     re-appears with the updated colour class.
 *
 * @param locator     Playwright locator
 * @param status      Target attendance status
 * @param attendeeName Display name of the attendee whose status to change
 */
export async function setMatchAttendance(
  locator: Locator,
  status: "attending" | "maybe" | "absent",
  attendeeName: string,
): Promise<void> {
  // Map status to the accessible name of the refinement icon button.
  // MUI renders aria-label from the SVG title; Playwright finds these by
  // role="button" with the SVG's accessible name.
  const refinementButtonLabel: Record<
    "attending" | "maybe" | "absent",
    RegExp
  > = {
    attending: /check/i,
    maybe: /help/i,
    absent: /close/i,
  };

  // Step 1: click the attendee's name button to open AttendeeRefinement.
  const attendeeBtn = locator.getByRole("button", { name: attendeeName });
  await attendeeBtn.waitFor({ state: "visible" });
  await attendeeBtn.click();

  // Step 2: click the correct icon button in the refinement view.
  const refinementBtn = locator.getByRole("button", {
    name: refinementButtonLabel[status],
  });
  await refinementBtn.waitFor({ state: "visible" });
  await refinementBtn.click();

  // Step 3: wait for the refinement view to close by waiting for the attendee
  // button to reappear with the expected MUI colour class.
  const expectedClass = statusToMuiColorClass[status];
  await expect(locator.getByRole("button", { name: attendeeName })).toHaveClass(
    new RegExp(expectedClass),
  );
}

/**
 * Verify that the attendee's button shows the expected colour for the given
 * attendance status.
 *
 * @param locator     Playwright locator
 * @param status      Expected attendance status
 * @param attendeeName Display name of the attendee to check
 */
export async function verifyMatchAttendanceState(
  locator: Locator,
  status: "attending" | "maybe" | "absent",
  attendeeName: string,
): Promise<void> {
  const expectedClass = statusToMuiColorClass[status];
  await expect(locator.getByRole("button", { name: attendeeName })).toHaveClass(
    new RegExp(expectedClass),
  );
}
