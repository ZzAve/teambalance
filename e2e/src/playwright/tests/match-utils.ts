import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW } from "./utils";

/**
 * Pick a date+time in the MUI MobileDateTimePicker dialog.
 *
 * Strategy: open dialog → switch to text-input view (pen icon) →
 *           type digits into the single combined field → confirm with OK.
 *
 * MUI v5 keyboard mode renders ONE combined input (nl locale: dd-mm-yyyy hh:mm).
 * The masked input auto-inserts separators when given digit-only input.
 */
async function pickDateTime(page: Page, date: Date) {
  const dateInput = page.getByRole("textbox", { name: /Choose date/ });
  await dateInput.waitFor({ state: "visible" });
  await dateInput.click();

  const dialog = page.getByRole("dialog");
  await dialog.waitFor({ state: "visible" });

  // Switch to text-input view
  const textInputToggle = dialog.getByRole("button", { name: /text input/i });
  await textInputToggle.click();

  // MUI v5 keyboard mode: ONE combined date-time input (nl locale, 24h).
  // Type digits only — the mask auto-inserts separators.
  const pad2 = (n: number) => String(n).padStart(2, "0");
  const digits = `${pad2(date.getDate())}${pad2(date.getMonth() + 1)}${date.getFullYear()}${pad2(date.getHours())}${pad2(date.getMinutes())}`;

  const combinedInput = dialog.getByRole("textbox");
  await combinedInput.waitFor({ state: "visible" });
  await combinedInput.click();
  // Move to start of the masked input before typing — Playwright's click()
  // lands in the centre of the element, leaving the cursor mid-string.
  await page.keyboard.press("Home");
  await combinedInput.pressSequentially(digits);

  await dialog.getByRole("button", { name: "OK", exact: true }).click();
  await dialog.waitFor({ state: "hidden" });
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

  // Wait for state update (small delay for API call)
  await page.waitForTimeout(300);
}
