import React from "react";
import { Button, Hidden } from "@material-ui/core";
import LockIcon from "@material-ui/icons/Lock";
import {authenticationManager} from "../utils/AuthenticationManager";

export const Logout = ({ handleRefresh }) => {
  const handleLogout = () => {
    authenticationManager.logout();
    handleRefresh()
  };

  return (
    <Button variant="contained" color="secondary" onClick={handleLogout}>
      <LockIcon />
      <Hidden xsDown>Logout</Hidden>
    </Button>
  );
};
