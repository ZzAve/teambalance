import React, { useEffect, useState } from "react";
import Button from "@mui/material/Button";
import { TeamBalanceTheme } from "../TenantContext";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";

const themeKey = "teambalance_theme";
const getInitialState: () => TeamBalanceTheme = () => {
  let item = localStorage.getItem(themeKey);
  if (item === null || item === "null") {
    // use system preference
    return window.matchMedia &&
      window.matchMedia("(prefers-color-scheme: dark)").matches
      ? TeamBalanceTheme.DARK
      : TeamBalanceTheme.LIGHT;
  }

  if (Object.values(TeamBalanceTheme).includes(item as TeamBalanceTheme)) {
    return item as TeamBalanceTheme;
  } else {
    console.error(
      "Someone is trying to corrupt the localstorage... I'm watching you!"
    );
    return TeamBalanceTheme.LIGHT;
  }
};

const saveTheme = (theme: TeamBalanceTheme) => {
  localStorage.setItem(themeKey, theme);
};

export const ThemeToggler = (props: {
  setTheme: (x: TeamBalanceTheme) => void;
}) => {
  const [theme, setTheme] = useState<TeamBalanceTheme>(getInitialState()); // save to localstorage

  useEffect(() => {
    props.setTheme(theme);
  }, [props.setTheme, theme]);
  const toggleTheme = () => {
    setTheme((x) => {
      const newTheme =
        x === TeamBalanceTheme.LIGHT
          ? TeamBalanceTheme.DARK
          : TeamBalanceTheme.LIGHT;
      saveTheme(newTheme);
      return newTheme;
    });
  };
  return (
    // TODO consider creating a XyzButton that accepts a clickhandler, icon and text to show for sm and up
    <Button variant="contained" onClick={toggleTheme}>
      {theme === TeamBalanceTheme.DARK ? <LightModeIcon /> : <DarkModeIcon />}
    </Button>
  );
};
