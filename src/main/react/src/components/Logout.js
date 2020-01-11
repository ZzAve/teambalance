import React from "react";
import { Button, Hidden } from "@material-ui/core";
import LockIcon from "@material-ui/icons/Lock";

export const Logout = ({ setSecret }) => {
  const handleLogout = () => {
    setSecret(null);
  };

  return (
    <Button variant="contained" color="secondary" onClick={handleLogout}>
      <LockIcon />
      <Hidden xsDown>Logout</Hidden>
    </Button>
  );
};
