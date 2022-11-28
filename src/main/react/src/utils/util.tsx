export const delay = (ms: number) => new Promise((res) => setTimeout(res, ms));

export const withLoading =  async <T extends any>(loadingStateSetter: (x: boolean) => void, func: () => T ) => {
  try {
    await loadingStateSetter(true);
    return await func();
  } finally {
    await loadingStateSetter(false);
  }
};

const dateOptions: Partial<Intl.DateTimeFormatOptions> = {
  // year: "numeric",
  month: "long",
  day: "2-digit",
  weekday: "long",
  timeZone: "Europe/Amsterdam",
};

const timeOptions: Partial<Intl.DateTimeFormatOptions> = {
  hour12: false,
  hour: "numeric",
  minute: "numeric",
  timeZone: "Europe/Amsterdam",
};
export const formattedDate = (dateTime: Date, dateOptionsOverrides: Partial<Intl.DateTimeFormatOptions> = {}) =>
  new Intl.DateTimeFormat("nl-NL", {
    ...dateOptions,
    ...dateOptionsOverrides,
  }).format(dateTime);

export const formattedTime = (dateTime: Date, timeOptionsOverrides: Partial<Intl.DateTimeFormatOptions> = {}) =>
  new Intl.DateTimeFormat("nl-NL", {
    ...timeOptions,
    ...timeOptionsOverrides,
  }).format(dateTime);

export type ViewType = "list" | "table"
export const ViewTypeOld = {
  List: "list",
  Table: "table",
};
Object.freeze(ViewTypeOld);

export const toBase64 = (text: string) => {
  const buf = Buffer.from(text, "utf-8");
  return buf.toString("base64");
};
