import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW, pickDateTime } from "./utils";

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
  await page.getByRole("button", { name: `Update event ${eventId}` }).click();

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
 * Set user's attendance status for a match
 */
export async function setMatchAttendance(
  page: Page,
  status: "attending" | "maybe" | "absent",
): Promise<void> {
  const buttonMap = {
    attending: /Aanwezig/i,
    maybe: /Misschien/i,
    absent: /Afwezig/i,
  };

  const button = page.getByRole("button", { name: buttonMap[status] });
  await button.waitFor({ state: "visible" });
  await button.click();

  // Wait for the button to reflect selected state (API response applied)
  await expect(button).toHaveAttribute("data-selected", "true");
}

/**
 * Verify match attendance button shows selected state
 */
export async function verifyMatchAttendanceState(
  page: Page,
  status: "attending" | "maybe" | "absent"
): Promise<void> {
  const buttonMap = {
    attending: /Aanwezig/i,
    maybe: /Misschien/i,
    absent: /Afwezig/i,
  };

  const button = page.getByRole("button", { name: buttonMap[status] });
  await expect(button).toHaveAttribute("data-selected", "true");
}
