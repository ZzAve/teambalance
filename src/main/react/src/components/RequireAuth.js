import { Redirect, useLocation } from "react-router-dom";
import React from "react";
import { authenticationManager } from "../utils/AuthenticationManager";

const isAuthenticated = () => {
  return authenticationManager.isAuthenticated() === true;
};

export const RequireAuth = ({ children, redirectTo = "/authenticate" }) => {
  const location = useLocation();
  return isAuthenticated() ? (
    children
  ) : (
    <Redirect
      to={{
        pathname: redirectTo,
        state: { from: location },
      }}
      push={false}
    />
  );
};
