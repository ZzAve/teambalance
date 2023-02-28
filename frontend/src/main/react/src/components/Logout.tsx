import React from "react";
import LockIcon from "@mui/icons-material/Lock";
import { authenticationManager } from "../utils/AuthenticationManager";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";

export const Logout = (props: { handleRefresh: () => void }) => {
  const handleLogout = () => {
    authenticationManager.logout();
    props.handleRefresh();
  };

  return (
    // TODO consider creating a XyzButton that accepts a clickhandler, icon and text to show for sm and up
    <Button variant="contained" color="secondary" onClick={handleLogout}>
      <LockIcon />
      <Typography
        variant={"button"}
        sx={{ display: { sm: "block", xs: "none" } }}
      >
        Logout
      </Typography>
    </Button>
  );
};
