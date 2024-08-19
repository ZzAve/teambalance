import React from "react";
import RefreshIcon from "@mui/icons-material/Refresh";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import { Hidden } from "@mui/material";

export const Refresh = (props: { handleRefresh: () => void }) => {
  return (
    // TODO consider creating a XyzButton that accepts a clickhandler, icon and text to show for sm and up
    <Button variant="contained" onClick={props.handleRefresh}>
      <RefreshIcon />
      <Hidden smDown>
        <Typography variant={"button"}>Refresh</Typography>
      </Hidden>
    </Button>
  );
};
