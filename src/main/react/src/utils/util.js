export const delay = (ms) => new Promise((res) => setTimeout(res, ms));

export const withLoading = async (loadingStateSetter, func) => {
  try {
    await loadingStateSetter(true);
    const x = await func(); // fault tolerant part
    return x;
    // }
    // catch (e) {
    //     console.error("Exception occurred by 'loading surrounded' func.",e);
    //     throw e
  } finally {
    await loadingStateSetter(false);
  }
};

const dateOptions = {
  // year: "numeric",
  month: "long",
  day: "2-digit",
  weekday: "long",
  timeZone: "Europe/Amsterdam",
};

const timeOptions = {
  hour12: false,
  hour: "numeric",
  minute: "numeric",
  timeZone: "Europe/Amsterdam",
};
export const formattedDate = (dateTime) =>
  new Intl.DateTimeFormat("nl-NL", dateOptions).format(dateTime);
export const formattedTime = (dateTime) =>
  new Intl.DateTimeFormat("nl-NL", timeOptions).format(dateTime);

export const ViewType = {
  List: "list",
  Table: "table",
};
Object.freeze(ViewType);
