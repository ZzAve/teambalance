import React from "react";
import RefreshIcon from "@material-ui/icons/Refresh";
import Button from "@material-ui/core/Button";
import Hidden from "@material-ui/core/Hidden";

export const Refresh = ({ handleRefresh }) => {
  return (
    <Button variant="contained" onClick={handleRefresh}>
      <RefreshIcon />
      <Hidden xsDown>Refresh</Hidden>
    </Button>
  );
};
