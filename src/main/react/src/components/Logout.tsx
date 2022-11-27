import React from "react";
import LockIcon from "@material-ui/icons/Lock";
import { authenticationManager } from "../utils/AuthenticationManager";
import Button from "@material-ui/core/Button";
import Hidden from "@material-ui/core/Hidden";

export const Logout = (props: { handleRefresh: () => void }) => {
  const handleLogout = () => {
    authenticationManager.logout();
    props.handleRefresh();
  };

  return (
    <Button variant="contained" color="secondary" onClick={handleLogout}>
      <LockIcon />
      <Hidden xsDown>Logout</Hidden>
    </Button>
  );
};
