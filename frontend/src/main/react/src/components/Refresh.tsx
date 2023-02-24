import React from "react";
import RefreshIcon from "@mui/icons-material/Refresh";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";

export const Refresh = (props: { handleRefresh: () => void }) => {
  return (
    // TODO consider creating a XyzButton that accepts a clickhandler, icon and text to show for sm and up
    <Button variant="contained" onClick={props.handleRefresh}>
      <RefreshIcon />
      <Typography
        variant={"button"}
        sx={{ display: { sm: "block", xs: "none" } }}
      >
        Refresh
      </Typography>
    </Button>
  );
};
