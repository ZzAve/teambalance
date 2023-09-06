import React from "react";
import Button from "@mui/material/Button";
import { TeamBalanceTheme } from "../TenantContext";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";

export const ThemeToggler = (props: {
  theme: TeamBalanceTheme;
  setTheme: (x: TeamBalanceTheme) => void;
}) => {
  const toggleTheme = () => {
    const newTheme =
      props.theme === TeamBalanceTheme.LIGHT
        ? TeamBalanceTheme.DARK
        : TeamBalanceTheme.LIGHT;
    // saveTheme(newTheme);
    // setTheme(newTheme);
    props.setTheme(newTheme);
    // return newTheme;
  };
  return (
    // TODO consider creating a XyzButton that accepts a clickhandler, icon and text to show for sm and up
    <Button variant="contained" onClick={toggleTheme}>
      {props.theme === TeamBalanceTheme.DARK ? (
        <LightModeIcon />
      ) : (
        <DarkModeIcon />
      )}
    </Button>
  );
};
