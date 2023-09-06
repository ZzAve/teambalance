import { TeamBalanceTheme } from "../TenantContext";
import { StorageObject } from "./storageService";
import { getStateFromLocalStorageFn, setStateToLocalStorage } from "./util";

const themeKey = "theme";
export const getTeamBalanceTheme: () => TeamBalanceTheme = () => {
  const typeGuard = (obj: StorageObject<unknown>) => {
    if (typeof obj.value === "string") {
      return Object.values(TeamBalanceTheme).includes(
        obj.value as TeamBalanceTheme
      );
    }

    console.error("*squints*. 🚩 someone's been hacking about ... ");
    return false;
  };
  const fallbackFn = () =>
    window.matchMedia &&
    window.matchMedia("(prefers-color-scheme: dark)").matches
      ? TeamBalanceTheme.DARK
      : TeamBalanceTheme.LIGHT;

  return getStateFromLocalStorageFn<TeamBalanceTheme>(
    themeKey,
    typeGuard,
    fallbackFn
  );
};

export const storeTeamBalanceTheme = (theme: TeamBalanceTheme) => {
  setStateToLocalStorage(themeKey, theme);
};
