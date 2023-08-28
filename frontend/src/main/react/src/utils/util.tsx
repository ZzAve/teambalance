export const delay = (ms: number) => new Promise((res) => setTimeout(res, ms));

export const withLoading = async <T extends any>(
  loadingStateSetter: (x: boolean) => void,
  func: () => T
) => {
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
export const formattedDate = (
  dateTime: Date,
  dateOptionsOverrides: Partial<Intl.DateTimeFormatOptions> = {}
) =>
  new Intl.DateTimeFormat("nl-NL", {
    ...dateOptions,
    ...dateOptionsOverrides,
  }).format(dateTime);

export const formattedTime = (
  dateTime: Date,
  timeOptionsOverrides: Partial<Intl.DateTimeFormatOptions> = {}
) =>
  new Intl.DateTimeFormat("nl-NL", {
    ...timeOptions,
    ...timeOptionsOverrides,
  }).format(dateTime);

export type ViewType = "list" | "table";
export const ViewTypeOld = {
  List: "list",
  Table: "table",
};
Object.freeze(ViewTypeOld);

export const toBase64 = (text: string) => {
  const buf = Buffer.from(text, "utf-8");
  return buf.toString("base64");
};

export interface EventsResponse<T> {
  totalSize: number;
  totalPages: number;
  page: number;
  size: number;
  events: T[];
}

/**
 * Sum all elements of a record that holds numbers for each T
 * It excluded the entries defined in exclusions
 */
export const sumRecord = <T extends string | number | symbol>(
  record: Record<T, number>,
  exclusions: Array<T>
) => {
  return Object.entries(record).reduce((cur, arr) => {
    const numberOfPlayers = exclusions.includes(arr[0] as T)
      ? 0
      : (arr[1] as number);
    return cur + numberOfPlayers;
  }, 0);
};
