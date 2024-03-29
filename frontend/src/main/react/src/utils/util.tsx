import { StorageObject, StorageService } from "./storageService";

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

/**
 * Given an array of items of type T, group them based on some property of T
 * @param items
 * @param keyFn function to find the key to group by for a given item
 * @return an object where each item is now list in the grouped and distinct sublist, split by keyFn.
 */
export const groupBy = <T extends object>(
  items: T[],
  keyFn: (item: T) => string
): { [x: string]: T[] } => {
  return items.reduce((acc: { [x: string]: T[] }, item: T) => {
    // Group initialization
    const key: string = keyFn(item);
    if (!acc[key]) {
      acc[key] = [];
    }

    // Grouping
    acc[key].push(item);

    return acc;
  }, {});
};

const storageService = new StorageService("teambalance", localStorage);

export const getStateFromLocalStorageFn = <T extends any>(
  key: string,
  typeGuard: (v: StorageObject<unknown>) => boolean,
  defaultValue: () => T
) => storageService.get<T>(key, typeGuard) ?? defaultValue();

export const getStateFromLocalStorage = <T extends any>(
  key: string,
  typeGuard: (v: StorageObject<unknown>) => boolean,
  defaultValue: T
) => {
  return storageService.get<T>(key, typeGuard) ?? defaultValue;
};

export const setStateToLocalStorage = <T extends any>(key: string, value: T) =>
  storageService.store<T>(key, value);

/**
 * Tries to do string interpolation a parameters based on named parameters.
 *
 * E.g. formatUnicorn("Hello {name}")({name: "Mother", job: "Baker"}) would result in "Hello Mother"
 */
export const formatUnicorn = (unicorn: string) => {
  let str = unicorn;
  return function (args?: { [p: string]: string }): string {
    if (args !== undefined) {
      let t = typeof arguments[0];
      let key;
      let args =
        "string" === t || "number" === t
          ? Array.prototype.slice.call(arguments)
          : arguments[0];

      for (key in args) {
        str = str.replace(new RegExp("\\{" + key + "\\}", "gi"), args[key]);
      }
    }

    return str;
  };
};
