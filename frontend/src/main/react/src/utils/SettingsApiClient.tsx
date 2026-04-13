import { ApiClient } from "./ApiClient";

const settingsClient = ApiClient();

interface SeasonResponse {
  startOfSeason: string;
}

const getSeasonStart: () => Promise<string> = async () => {
  const data = await settingsClient.call("settings/season");
  return (data as SeasonResponse).startOfSeason;
};

const updateSeasonStart: (startOfSeason: string) => Promise<string> = async (
  startOfSeason: string
) => {
  const data = await settingsClient.callWithBody(
    "settings/season",
    { startOfSeason },
    { method: "PUT" }
  );
  return (data as SeasonResponse).startOfSeason;
};

export const settingsApiClient = {
  getSeasonStart,
  updateSeasonStart,
};
