import { TeamBalanceTheme } from "../TenantContext";
import { StorageObject } from "./storageService";
import {
  getStateFromLocalStorage,
  getStateFromLocalStorageFn,
  setStateToLocalStorage,
  ViewType,
} from "./util";

const themeKey = "theme";
const viewTypeKey = "view_type";
const allAttendeesExpanded = "all_attendees_expanded";
const attendeeSummaryExpanded = "attendees_summary_expanded";
export const getTeamBalanceThemePreference: () => TeamBalanceTheme = () => {
  const typeGuard = (obj: StorageObject<unknown>) => {
    const potentialTheme = obj.value;
    return (
      typeof potentialTheme === "string" &&
      Object.values(TeamBalanceTheme).includes(
        potentialTheme as TeamBalanceTheme
      )
    );
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

export const storeTeamBalanceThemePreference = (theme: TeamBalanceTheme) => {
  setStateToLocalStorage(themeKey, theme);
};

export const getViewTypePreference: () => ViewType = () => {
  const typeGuard = (obj: StorageObject<unknown>) => {
    const potentialViewType = obj.value;
    return (
      typeof potentialViewType === "string" &&
      ["list", "table"].includes(potentialViewType)
    );
  };

  return getStateFromLocalStorage<ViewType>(viewTypeKey, typeGuard, "list");
};

export const storeViewTypePreference = (viewType: ViewType) => {
  setStateToLocalStorage(viewTypeKey, viewType);
};

export const getAllAttendeesExpandedPreference: () => boolean = () => {
  const typeGuard = (obj: StorageObject<unknown>) => {
    const potentialViewType = obj.value;
    return typeof potentialViewType === "boolean";
  };

  return getStateFromLocalStorage<boolean>(
    allAttendeesExpanded,
    typeGuard,
    false
  );
};
export const storeAllAttendeesExpandedPreference = (expanded: boolean) => {
  setStateToLocalStorage(allAttendeesExpanded, expanded);
};

export const getAttendeeSummaryInitiallyExpanded: () => boolean = () => {
  const typeGuard = (obj: StorageObject<unknown>) => {
    const potentialViewType = obj.value;
    return typeof potentialViewType === "boolean";
  };

  return getStateFromLocalStorage<boolean>(
    attendeeSummaryExpanded,
    typeGuard,
    false
  );
};
export const storeAttendeeSummaryInitiallyExpanded = (expanded: boolean) => {
  setStateToLocalStorage(attendeeSummaryExpanded, expanded);
};
