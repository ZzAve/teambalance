export const ensure = <T>(
  value: T | undefined,
  name: string | undefined = "value",
) => {
  return (
    value ??
    (() => {
      throw new Error(
        `Expected non-null values for '${name}' was unexpectedly null`,
      );
    })()
  );
};

export const HOST: string = ensure(process.env.HOST, "host");
export const PASSWORD = ensure(process.env.PASSWORD, "PASSWORD");

export const NOW = new Date();

/**
 * Fetch the current start-of-season date from the backend settings API.
 * The endpoint is public (no secret required).
 */
export async function getStartOfSeason(
  request: import("@playwright/test").APIRequestContext,
): Promise<Date> {
  const apiBase = HOST.replace(/\/+$/, "");
  const response = await request.get(`${apiBase}/api/settings/season`);
  if (!response.ok()) {
    throw new Error(
      `Failed to fetch start-of-season: ${response.status()} ${response.statusText()}`,
    );
  }
  const body = (await response.json()) as { startOfSeason: string };
  return new Date(body.startOfSeason);
}

export const addHours = (date: Date, hours: number): Date => {
  const result = new Date(date);
  result.setHours(result.getHours() + hours);
  return result;
};

export const addDays = (date: Date, days: number): Date => {
  const result = new Date(date);
  result.setHours(result.getHours() + days * 24);
  return result;
};

/**
 * Pick a date+time in the MUI MobileDateTimePicker dialog.
 *
 * Strategy: open dialog → switch to text-input view (pen icon) →
 *           type digits into the single combined field → confirm with OK.
 *
 * MUI v5 keyboard mode renders ONE combined input (nl locale: dd-mm-yyyy hh:mm).
 * The masked input auto-inserts separators when given digit-only input.
 */
export async function pickDateTime(
  page: import("@playwright/test").Page,
  date: Date,
): Promise<void> {
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
