import React from "react";
import { Button, Hidden } from "@material-ui/core";
import RefreshIcon from "@material-ui/icons/Refresh";

export const Refresh = ({ handleRefresh }) => {
  return (
    <Button variant="contained" onClick={handleRefresh}>
      <RefreshIcon />
      <Hidden xsDown>Refresh</Hidden>
    </Button>
  );
};
