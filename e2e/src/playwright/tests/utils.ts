import { APIRequestContext } from "@playwright/test";

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

const API_SECRET = Buffer.from("teambalance").toString("base64");
const apiHeaders = {
  Host: "frontend:3000",
  "X-Secret": API_SECRET,
  "Content-Type": "application/json",
};

export const NOW = new Date();

/**
 * Fetch the current start-of-season date from the backend config API.
 */
export async function getStartOfSeason(
  request: APIRequestContext,
): Promise<Date> {
  const response = await request.get(`${HOST}/api/config/season`, {
    headers: apiHeaders,
  });
  if (!response.ok()) {
    throw new Error(
      `Failed to fetch start-of-season: ${response.status()} ${response.statusText()}`,
    );
  }
  const body = (await response.json()) as { startOfSeason: string };
  const result = new Date(body.startOfSeason);
  if (isNaN(result.getTime())) {
    throw new Error(
      `getStartOfSeason: invalid date in response: ${body.startOfSeason}`,
    );
  }
  return result;
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

  const combinedInput = dialog.getByRole("textbox", { name: "Datum / tijd" });
  await combinedInput.waitFor({ state: "visible" });
  await combinedInput.click();
  await combinedInput.fill(digits);

  await dialog.getByRole("button", { name: "OK", exact: true }).click();
  await dialog.waitFor({ state: "hidden" });
}
