import React from "react";
import RefreshIcon from "@material-ui/icons/Refresh";
import Button from "@material-ui/core/Button";
import Hidden from "@material-ui/core/Hidden";

export const Refresh = (props: { handleRefresh: () => void }) => {
  return (
    <Button variant="contained" onClick={props.handleRefresh}>
      <RefreshIcon />
      <Hidden xsDown>Refresh</Hidden>
    </Button>
  );
};
